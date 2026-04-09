package com.finflow.notification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;
    private String recipientEmail;
    private String recipientName;

    @Column(columnDefinition = "TEXT")
    private String messageContent;

    private LocalDateTime sentAt;
}
