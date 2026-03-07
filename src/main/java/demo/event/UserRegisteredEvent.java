package demo.event;

import java.time.Instant;

public class UserRegisteredEvent {

    private String eventId;
    private Instant occurredAt;
    private String userId;
    private String email;
    private String tokenId;
    private String tokenClear;

    public UserRegisteredEvent() {}

    public UserRegisteredEvent(String eventId,
                               Instant occurredAt,
                               String userId,
                               String email,
                               String tokenId,
                               String tokenClear) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.userId = userId;
        this.email = email;
        this.tokenId = tokenId;
        this.tokenClear = tokenClear;
    }

    // getters et setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenClear() {
        return tokenClear;
    }

    public void setTokenClear(String tokenClear) {
        this.tokenClear = tokenClear;
    }
}