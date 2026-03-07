package demo.event;

import java.time.Instant;

public class EmailVerifiedEvent {

    private String eventId;
    private Instant occurredAt;
    private String userId;

    public EmailVerifiedEvent() {
    }

    public EmailVerifiedEvent(String eventId, Instant occurredAt, String userId) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}