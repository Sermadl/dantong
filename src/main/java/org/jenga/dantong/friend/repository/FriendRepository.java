package org.jenga.dantong.friend.repository;

import org.jenga.dantong.friend.model.entity.Friend;
import org.jenga.dantong.friend.model.entity.FriendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FriendRepository extends JpaRepository<Friend, Long> {

    Page<Friend> findByUserStudentIdAndStatusAndIsFrom(Pageable pageable, String studentId, FriendStatus status, Boolean isFrom);

    Page<Friend> findByUserStudentIdAndStatus(Pageable pageable, String studentId, FriendStatus status);

    Optional<Friend> findByUserStudentIdAndFriendStudentIdAndStatus(String userId, String friendStudentId, FriendStatus status);

    Optional<Friend> findByUserStudentIdAndFriendStudentIdAndStatusAndIsFrom(String userId, String friendStudentId, FriendStatus status, Boolean isFrom);

    Optional<Friend> findByIdAndUserStudentIdAndStatus(Long friendId, String userStudentId, FriendStatus status);
}
