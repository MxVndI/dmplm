package com.diplom.notification.domain.service;

import com.diplom.notification.persistance.entity.NotificationCampaignEntity;
import com.diplom.notification.persistance.entity.NotificationDeliveryEntity;
import com.diplom.notification.persistance.entity.CampaignStatus;
import com.diplom.notification.persistance.entity.DeliveryStatus;
import com.diplom.notification.persistance.repository.NotificationCampaignRepository;
import com.diplom.notification.persistance.repository.NotificationDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final NotificationCampaignRepository campaignRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final EmailService emailService;
    private final TelegramService telegramService;

    @Async
    public void dispatchCampaign(String campaignId) {
        NotificationCampaignEntity campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        List<NotificationDeliveryEntity> pendingDeliveries =
                deliveryRepository.findByCampaignIdAndStatusAndChannelOrderByCreatedAt(
                        campaignId, DeliveryStatus.PENDING, campaign.getChannel());

        switch (campaign.getChannel().toUpperCase()) {
            case "EMAIL" -> dispatchEmail(campaign, pendingDeliveries);
            case "TELEGRAM" -> dispatchTelegram(campaign, pendingDeliveries);
            case "BOTH" -> {
                dispatchEmail(campaign, deliveryRepository.findByCampaignIdAndStatusAndChannelOrderByCreatedAt(
                        campaignId, DeliveryStatus.PENDING, "EMAIL"));
                dispatchTelegram(campaign, deliveryRepository.findByCampaignIdAndStatusAndChannelOrderByCreatedAt(
                        campaignId, DeliveryStatus.PENDING, "TELEGRAM"));
            }
            default -> log.warn("Unknown notification channel: {}", campaign.getChannel());
        }
        refreshCampaignCounters(campaign);
    }

    private void dispatchEmail(NotificationCampaignEntity campaign, List<NotificationDeliveryEntity> deliveries) {
        for (NotificationDeliveryEntity delivery : deliveries) {
            try {
                emailService.sendCampaignEmail(
                        delivery.getUserId(),
                        delivery.getId(),
                        campaign.getSubject(),
                        campaign.getBody()
                );
                log.debug("Email delivery {} processed for user {}", delivery.getId(), delivery.getUserId());
            } catch (Exception e) {
                log.error("Error sending email for delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }

    private void dispatchTelegram(NotificationCampaignEntity campaign, List<NotificationDeliveryEntity> deliveries) {
        for (NotificationDeliveryEntity delivery : deliveries) {
            try {
                telegramService.sendTelegramMessage(
                        delivery.getUserId(),
                        delivery.getId(),
                        campaign.getBody()
                );
                log.debug("Telegram delivery {} processed for user {}", delivery.getId(), delivery.getUserId());
            } catch (Exception e) {
                log.error("Error sending Telegram for delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }

    public void processPendingCampaigns() {
        List<NotificationCampaignEntity> pending = campaignRepository.findByStatus(CampaignStatus.SENT);
        log.info("Processing {} pending campaigns", pending.size());

        for (NotificationCampaignEntity campaign : pending) {
            dispatchCampaign(campaign.getId());
        }
    }

    private void refreshCampaignCounters(NotificationCampaignEntity campaign) {
        long deliveredEmail = deliveryRepository.countByCampaignIdAndChannelAndStatus(
                campaign.getId(), "EMAIL", DeliveryStatus.DELIVERED);
        long deliveredTelegram = deliveryRepository.countByCampaignIdAndChannelAndStatus(
                campaign.getId(), "TELEGRAM", DeliveryStatus.DELIVERED);
        long failedEmail = deliveryRepository.countByCampaignIdAndChannelAndStatus(
                campaign.getId(), "EMAIL", DeliveryStatus.FAILED);
        long failedTelegram = deliveryRepository.countByCampaignIdAndChannelAndStatus(
                campaign.getId(), "TELEGRAM", DeliveryStatus.FAILED);

        campaign.setSentCount((int) (deliveredEmail + deliveredTelegram));
        campaign.setFailedCount((int) (failedEmail + failedTelegram));
        campaignRepository.save(campaign);
    }
}
