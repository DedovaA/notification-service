package notification_service.kafka;

public record UserEvent(Operation operation, String email) {
    public enum Operation { CREATE, DELETE }
}
