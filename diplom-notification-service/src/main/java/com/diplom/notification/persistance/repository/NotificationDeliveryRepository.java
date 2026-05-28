package com.diplom.notification.persistance.repository;

import com.diplom.notification.persistance.entity.NotificationDeliveryEntity;
import com.diplom.notification.persistance.entity.DeliveryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationDeliveryRepository extends MongoRepository<NotificationDeliveryEntity, String> {
    List<NotificationDeliveryEntity> findByCampaignId(String campaignId);
    long countByCampaignId(String campaignId);
    long countByCampaignIdAndStatus(String campaignId, DeliveryStatus status);
    long countByCampaignIdAndChannelAndStatus(String campaignId, String channel, DeliveryStatus status);
    void deleteByCampaignId(String campaignId);
    List<NotificationDeliveryEntity> findByCampaignIdAndStatusAndChannelOrderByCreatedAt(
            String campaignId, DeliveryStatus status, String channel);
}
