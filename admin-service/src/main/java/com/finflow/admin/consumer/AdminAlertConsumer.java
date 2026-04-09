package com.finflow.admin.consumer;

import com.finflow.admin.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AdminAlertConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMIN_ALERTS)
    public void consumeAdminAlert(Map<String, Object> message) {
        String eventType = (String) message.getOrDefault("eventType", "UNKNOWN");
        String applicationId = (String) message.getOrDefault("applicationId", "N/A");
        String loanId = (String) message.getOrDefault("loanId", "N/A");
        
        // We only care about events that require Admin action
        if ("LOAN_SUBMITTED".equals(eventType)) {
            log.info("📢 [ADMIN ALERT] New Loan Application Received!");
            log.info("Application ID : {}", loanId);
            log.info("Status         : SUBMITTED - Needs Review");
            log.info("-------------------------------------------------");
        } 
        else if ("DOCUMENT_UPLOADED".equals(eventType)) {
            String docType = (String) message.getOrDefault("documentType", "N/A");
            log.info("📄 [ADMIN ALERT] New Document Uploaded for Review");
            log.info("Application ID : {}", applicationId);
            log.info("Document Type  : {}", docType);
            log.info("-------------------------------------------------");
        }
    }
}
