package org.jenga.dantong.friend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.friend.exception.*;
import org.jenga.dantong.friend.model.dto.response.FriendListResponse;
import org.jenga.dantong.friend.model.dto.response.RequestListResponse;
import org.jenga.dantong.friend.model.entity.Friend;
import org.jenga.dantong.friend.model.entity.FriendStatus;
import org.jenga.dantong.friend.repository.FriendRepository;
import org.jenga.dantong.post.exception.PermissionDeniedException;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public void sendRequest(String studentId, Long userId) {
        User toUser = userRepository.findByStudentId(studentId)
                .orElseThrow(UserNotFoundException::new);

        User fromUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (studentId.equals(fromUser.getStudentId())) {
            throw new NotAvailableRequestException();
        }
        if (friendRepository.findByUserStudentIdAndFriendStudentIdAndStatus(fromUser.getStudentId(), studentId, FriendStatus.ACCEPT).isPresent()) {
            throw new AlreadyFriendException();
        }
        if (friendRepository.findByUserStudentIdAndFriendStudentIdAndStatusAndIsFrom(studentId, fromUser.getStudentId(), FriendStatus.WAITING, false).isPresent()) {
            throw new FriendAlreadySentRequestException();
        }
        if (friendRepository.findByUserStudentIdAndFriendStudentIdAndStatus(fromUser.getStudentId(), studentId, FriendStatus.WAITING).isPresent()) {
            throw new AlreadySentRequestException();
        }

        /**
         * 보내는 쪽
         */
        Friend friendTo = Friend.builder()
                .friend(toUser)
                .userStudentId(fromUser.getStudentId())
                .friendStudentId(toUser.getStudentId())
                .status(FriendStatus.WAITING)
                .isFrom(false)
                .build();

        /**
         * 받는 쪽
         */
        Friend friendFrom = Friend.builder()
                .friend(fromUser)
                .userStudentId(toUser.getStudentId())
                .friendStudentId(fromUser.getStudentId())
                .status(FriendStatus.WAITING)
                .isFrom(true)
                .build();

        toUser.getFriendList().add(friendFrom);
        fromUser.getFriendList().add(friendTo);
        friendRepository.save(friendFrom);
        friendRepository.save(friendTo);

        friendTo.setCounterpartId(friendFrom.getId());
        friendFrom.setCounterpartId(friendTo.getId());
    }

    public Page<RequestListResponse> getRequestList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Page<Friend> requestList = friendRepository.findByUserStudentIdAndStatusAndIsFrom(pageable, user.getStudentId(), FriendStatus.WAITING, true);

        return getRequestResponses(requestList);
    }

    @Transactional
    public void acceptRequest(Long friendshipId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Friend toUser = friendRepository.findById(friendshipId)
                .orElseThrow(FriendshipNotFoundException::new);
        Friend fromUser = friendRepository.findById(toUser.getCounterpartId())
                .orElseThrow(FriendshipNotFoundException::new);

        if (!user.getStudentId().equals(toUser.getUserStudentId())) {
            log.info(user.getStudentId());
            log.info(toUser.getUserStudentId());
            throw new PermissionDeniedException();
        }

        toUser.acceptRequest();
        fromUser.acceptRequest();
    }

    public Page<FriendListResponse> getFriendList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Page<Friend> friendList = friendRepository.findByUserStudentIdAndStatus(pageable, user.getStudentId(), FriendStatus.ACCEPT);

        return getFriendListResponses(friendList);
    }

    private Page<RequestListResponse> getRequestResponses(Page<Friend> requests) {
        return requests.map(currFriend -> {
            String studentId = currFriend.getFriend().getStudentId();
            String name = currFriend.getFriend().getName();
            Long friendshipId = currFriend.getId();
            return new RequestListResponse(studentId, name, friendshipId);
        });
    }

    private Page<FriendListResponse> getFriendListResponses(Page<Friend> friends) {
        return friends.map(currFriend -> {
            String studentId = currFriend.getFriend().getStudentId();
            String name = currFriend.getFriend().getName();
            return new FriendListResponse(studentId, name);
        });
    }
}
