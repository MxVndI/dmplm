package com.diplom.notification.domain.service;

import com.diplom.notification.constant.AppConstants;
import com.diplom.notification.persistance.entity.CampaignStatus;
import com.diplom.notification.persistance.entity.NotificationCampaignEntity;
import com.diplom.notification.persistance.entity.NotificationDeliveryEntity;
import com.diplom.notification.persistance.entity.DeliveryStatus;
import com.diplom.notification.persistance.repository.NotificationCampaignRepository;
import com.diplom.notification.persistance.repository.NotificationDeliveryRepository;
import com.diplom.notification.rest.dto.CreateAbCampaignDto;
import com.diplom.notification.rest.dto.CreateCampaignDto;
import com.diplom.notification.utils.ShopUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final NotificationCampaignRepository campaignRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final ShopUserClient shopUserClient;
    private final NotificationDispatcher dispatcher;

    public List<NotificationCampaignEntity> findAll() {
        return campaignRepository.findAll();
    }

    public NotificationCampaignEntity findById(String id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.CAMPAIGN_NOT_FOUND + id));
    }

    public NotificationCampaignEntity create(CreateCampaignDto dto) {
        NotificationCampaignEntity campaign = new NotificationCampaignEntity();
        campaign.setName(dto.getName());
        campaign.setChannel(dto.getChannel());
        campaign.setSubject(dto.getSubject());
        campaign.setBody(dto.getBody());
        campaign.setTargetType(dto.getTargetType());
        campaign.setTargetUserIds(dto.getTargetUserIds());
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setCreatedAt(LocalDateTime.now());
        return campaignRepository.save(campaign);
    }

    public List<NotificationCampaignEntity> createAbPair(CreateAbCampaignDto dto) {
        NotificationCampaignEntity campaignA = new NotificationCampaignEntity();
        campaignA.setName(dto.getBaseName() + AppConstants.VARIANT_A_SUFFIX);
        campaignA.setChannel(dto.getChannel());
        campaignA.setSubject(dto.getSubjectA());
        campaignA.setBody(dto.getBodyA());
        campaignA.setTargetType(AppConstants.TARGET_SPECIFIC);
        campaignA.setTestId(dto.getTestId());
        campaignA.setTestVariant(AppConstants.VARIANT_A);
        campaignA.setStatus(CampaignStatus.DRAFT);
        campaignA.setCreatedAt(LocalDateTime.now());

        NotificationCampaignEntity campaignB = new NotificationCampaignEntity();
        campaignB.setName(dto.getBaseName() + AppConstants.VARIANT_B_SUFFIX);
        campaignB.setChannel(dto.getChannel());
        campaignB.setSubject(dto.getSubjectB());
        campaignB.setBody(dto.getBodyB());
        campaignB.setTargetType(AppConstants.TARGET_SPECIFIC);
        campaignB.setTestId(dto.getTestId());
        campaignB.setTestVariant(AppConstants.VARIANT_B);
        campaignB.setStatus(CampaignStatus.DRAFT);
        campaignB.setCreatedAt(LocalDateTime.now());

        campaignRepository.save(campaignA);
        campaignRepository.save(campaignB);

        return List.of(campaignA, campaignB);
    }

    public NotificationCampaignEntity send(String campaignId) {
        NotificationCampaignEntity campaign = findById(campaignId);
        if (campaign.getStatus() == CampaignStatus.SENT) {
            throw new IllegalStateException(AppConstants.CAMPAIGN_ALREADY_SENT);
        }

        List<String> targetUserIds = resolveTargetUsers(campaign);
        log.info("Sending campaign '{}' to {} users", campaign.getName(), targetUserIds.size());

        for (String userId : targetUserIds) {
            if (AppConstants.CHANNEL_BOTH.equalsIgnoreCase(campaign.getChannel())) {
                createDelivery(campaignId, userId, AppConstants.CHANNEL_EMAIL);
                createDelivery(campaignId, userId, AppConstants.CHANNEL_TELEGRAM);
            } else {
                createDelivery(campaignId, userId, campaign.getChannel());
            }
        }

        campaign.setStatus(CampaignStatus.SENT);
        campaign.setSentAt(LocalDateTime.now());
        NotificationCampaignEntity saved = campaignRepository.save(campaign);

        dispatcher.dispatchCampaign(campaignId);

        return saved;
    }

    public Map<String, Long> getDeliveryStats(String campaignId) {
        long total = deliveryRepository.countByCampaignId(campaignId);
        long successful = deliveryRepository.countByCampaignIdAndStatus(campaignId, DeliveryStatus.DELIVERED);
        long failed = deliveryRepository.countByCampaignIdAndStatus(campaignId, DeliveryStatus.FAILED);
        long pending = deliveryRepository.countByCampaignIdAndStatus(campaignId, DeliveryStatus.PENDING);

        return Map.of(
                "total", total,
                "delivered", successful,
                "failed", failed,
                "pending", pending
        );
    }

    public List<Map<String, Object>> getAbComparisonStats(String testId) {
        List<NotificationCampaignEntity> campaigns = campaignRepository.findByTestId(testId);
        return campaigns.stream().map(campaign -> {
            Map<String, Long> stats = getDeliveryStats(campaign.getId());
            return Map.of(
                    "campaignId", campaign.getId(),
                    "campaignName", (Object) campaign.getName(),
                    "variant", campaign.getTestVariant(),
                    "stats", stats
            );
        }).collect(Collectors.toList());
    }

    public void delete(String campaignId) {
        NotificationCampaignEntity campaign = findById(campaignId);
        if (campaign.getStatus() == CampaignStatus.SENT) {
            throw new IllegalStateException(AppConstants.CANNOT_DELETE_SENT_CAMPAIGN);
        }
        deliveryRepository.deleteByCampaignId(campaignId);
        campaignRepository.deleteById(campaignId);
        log.info("Deleted campaign '{}'", campaign.getName());
    }

    private List<String> resolveTargetUsers(NotificationCampaignEntity campaign) {
        List<String> userIds = new ArrayList<>();

        if (AppConstants.TARGET_SPECIFIC.equalsIgnoreCase(campaign.getTargetType())) {
            if (campaign.getTestId() != null) {
                userIds.addAll(shopUserClient.getUsersByTest(campaign.getTestId(), campaign.getTestVariant()));
            } else if (campaign.getTargetUserIds() != null) {
                userIds.addAll(campaign.getTargetUserIds());
            }
        } else if (AppConstants.TARGET_ALL.equalsIgnoreCase(campaign.getTargetType())) {
            userIds.addAll(shopUserClient.getAllUserIds());
        }

        return userIds;
    }

    private void createDelivery(String campaignId, String userId, String channel) {
        NotificationDeliveryEntity delivery = new NotificationDeliveryEntity();
        delivery.setCampaignId(campaignId);
        delivery.setUserId(userId);
        delivery.setChannel(channel);
        delivery.setStatus(DeliveryStatus.PENDING);
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
    }
}
