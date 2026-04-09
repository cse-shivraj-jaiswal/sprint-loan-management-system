package com.finflow.notification.consumer;

import com.finflow.notification.config.RabbitMQConfig;
import com.finflow.notification.dto.NotificationEvent;
import com.finflow.notification.model.NotificationHistory;
import com.finflow.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void consumeNotification(NotificationEvent event) {
        log.info("--- 📥 EVENT RECEIVED BY NOTIFICATION-SERVICE ---");
        
        String eventType = event.getEventType();
        String email = event.getEmail();
        String name = event.getName();
        String messageContent = event.getMessage();
        String docType = event.getDocumentType();

        log.info("Event Type: {}", eventType);
        
        // 💾 SAVE TO DATABASE
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .eventType(eventType)
                    .recipientEmail(email)
                    .recipientName(name)
                    .messageContent(messageContent)
                    .sentAt(LocalDateTime.now())
                    .build();
            
            notificationRepository.save(history);
            log.info("✅ Notification history saved to database.");
        } catch (Exception e) {
            log.error("❌ Failed to save notification history: {}", e.getMessage());
        }

        // 📧 MOCK EMAIL LOGS
        log.info("*************************************************");
        log.info("TO      : {}", email);
        log.info("SUBJECT : FinFlow Update - {}", eventType.replace("_", " "));
        log.info("BODY    : Hi {}, \n          {}", name, messageContent);
        
        if (docType != null && !docType.isEmpty()) {
            log.info("DOC TYPE: {}", docType);
        }

        log.info("*************************************************");
        log.info("--- 📤 MOCK EMAIL LOG COMPLETED ---");
    }
}
