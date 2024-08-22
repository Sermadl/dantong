package org.jenga.dantong.notification.repository;

import org.jenga.dantong.notification.model.entity.FcmNotification;
import org.jenga.dantong.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmNotificationRepository extends JpaRepository<FcmNotification, Long> {
    Page<FcmNotification> findByUser(User user, Pageable pageable);
}
