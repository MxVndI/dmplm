package com.diplom.notification.persistance.repository;

import com.diplom.notification.persistance.entity.NotificationCampaignEntity;
import com.diplom.notification.persistance.entity.CampaignStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationCampaignRepository extends MongoRepository<NotificationCampaignEntity, String> {
    List<NotificationCampaignEntity> findByTestId(String testId);
    List<NotificationCampaignEntity> findByTestIdOrderByCreatedAtDesc(String testId);
    List<NotificationCampaignEntity> findAllByOrderByCreatedAtDesc();
    List<NotificationCampaignEntity> findByStatus(CampaignStatus status);
}
