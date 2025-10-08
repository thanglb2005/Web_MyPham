package vn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "ix_chat_message_room_time", columnList = "room_id,sent_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", length = 100, nullable = false)
    private String roomId;

    @Column(name = "sender", length = 255, nullable = false)
    private String sender;

    @Column(name = "sender_type", length = 50, nullable = false)
    private String senderType;

    @Column(name = "message_type", length = 20, nullable = false)
    private String messageType;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    // epoch millis for easy ordering and API compatibility
    @Column(name = "sent_at", nullable = false)
    private Long sentAt;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSentAt() {
        return sentAt;
    }

    public void setSentAt(Long sentAt) {
        this.sentAt = sentAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
}




