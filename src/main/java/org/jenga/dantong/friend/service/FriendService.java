package org.jenga.dantong.friend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jenga.dantong.friend.exception.*;
import org.jenga.dantong.friend.model.dto.response.FriendListResponse;
import org.jenga.dantong.friend.model.dto.response.RequestListResponse;
import org.jenga.dantong.friend.model.dto.response.SubmitFriendListResponse;
import org.jenga.dantong.friend.model.entity.Friend;
import org.jenga.dantong.friend.model.entity.FriendStatus;
import org.jenga.dantong.friend.repository.FriendRepository;
import org.jenga.dantong.post.exception.PermissionDeniedException;
import org.jenga.dantong.post.exception.PostNofFoundException;
import org.jenga.dantong.post.model.entity.Post;
import org.jenga.dantong.post.repository.PostRepository;
import org.jenga.dantong.survey.exception.SurveySubmitNotFoundException;
import org.jenga.dantong.survey.model.dto.response.TicketResponse;
import org.jenga.dantong.survey.model.entity.Survey;
import org.jenga.dantong.survey.repository.SurveySubmitRepository;
import org.jenga.dantong.survey.service.SurveyService;
import org.jenga.dantong.user.exception.UserNotFoundException;
import org.jenga.dantong.user.model.entity.Major;
import org.jenga.dantong.user.model.entity.User;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final SurveyService surveyService;
    private final PostRepository postRepository;
    private final SurveySubmitRepository surveySubmitRepository;

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
        Friend friendFrom = Friend.builder()
                .friend(toUser)
                .userStudentId(fromUser.getStudentId())
                .friendStudentId(toUser.getStudentId())
                .status(FriendStatus.WAITING)
                .isFrom(true)
                .build();

        /**
         * 받는 쪽
         */
        Friend friendTo = Friend.builder()
                .friend(fromUser)
                .userStudentId(toUser.getStudentId())
                .friendStudentId(fromUser.getStudentId())
                .status(FriendStatus.WAITING)
                .isFrom(false)
                .build();

        toUser.getFriendList().add(friendTo);
        fromUser.getFriendList().add(friendFrom);
        friendRepository.save(friendFrom);
        friendRepository.save(friendTo);

        friendTo.setCounterpartId(friendFrom.getId());
        friendFrom.setCounterpartId(friendTo.getId());
    }

    public Page<RequestListResponse> getRequestList(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Page<Friend> requestList = friendRepository.findByUserStudentIdAndStatusAndIsFrom(pageable, user.getStudentId(), FriendStatus.WAITING, false);

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
            Major major = currFriend.getFriend().getMajor();
            Long friendshipId = currFriend.getId();
            return new RequestListResponse(studentId, name, major, friendshipId);
        });
    }

    private Page<FriendListResponse> getFriendListResponses(Page<Friend> friends) {
        return friends.map(currFriend -> {
            Long friendId = currFriend.getId();
            String studentId = currFriend.getFriend().getStudentId();
            Major major = currFriend.getFriend().getMajor();
            String name = currFriend.getFriend().getName();

            return new FriendListResponse(friendId, studentId, major, name);
        });
    }

    public List<TicketResponse> viewSubmitByStudentId(String studentId, Long userId) {
        User friend = userRepository.findByStudentId(studentId)
                .orElseThrow(UserNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (friendRepository.findByUserStudentIdAndFriendStudentIdAndStatus(user.getStudentId(), studentId, FriendStatus.ACCEPT).isEmpty())
            throw new FriendshipNotFoundException();

        return surveyService.getTickets(friend.getId());
    }

    public List<FriendListResponse> viewSubmitByPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostNofFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Survey survey = post.getSurvey();

        return surveySubmitRepository.findBySurvey(survey).stream().map(currSubmit -> {
                    String studentId = currSubmit.getUser().getStudentId();
                    Major major = currSubmit.getUser().getMajor();
                    String name = currSubmit.getUser().getName();

                    return new FriendListResponse(studentId, major, name);
                })
                .map(currFriend -> {
                    User friend = userRepository.findByStudentId(currFriend.getUserStudentId())
                            .orElseThrow(UserNotFoundException::new);
                    return new SubmitFriendListResponse(friend.getStudentId(), friend.getMajor(), friend.getName());
                }).toList();
    }

    public void deleteRequest(Long friendId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Friend friend = friendRepository.findByIdAndUserStudentIdAndStatus(friendId, user.getStudentId(), FriendStatus.WAITING)
                .orElseThrow(FriendshipNotFoundException::new);
        friendRepository.deleteById(friend.getId());
        friendRepository.deleteById(friend.getCounterpartId());
    }

    public void deleteFriend(Long friendshipId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        log.info(String.valueOf(friendshipId));
        log.info(user.getStudentId());
        Friend friend = friendRepository.findByIdAndUserStudentIdAndStatus(friendshipId, user.getStudentId(), FriendStatus.ACCEPT)
                .orElseThrow(FriendshipNotFoundException::new);
        friendRepository.deleteById(friend.getId());
        friendRepository.deleteById(friend.getCounterpartId());

    }
}
