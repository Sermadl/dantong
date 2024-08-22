package org.jenga.dantong.notification.repository;

import org.jenga.dantong.notification.model.entity.FcmNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmNotificationRepository extends JpaRepository<FcmNotification, Long> {
}
