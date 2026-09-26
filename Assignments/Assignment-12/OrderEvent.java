package masr;

import java.time.LocalDateTime;

public record OrderEvent(String orderId,
                         Order order,
                         OrderStatus previousStatus,
                         OrderStatus newStatus,
                         LocalDateTime occurredAt,
                         String detail) {

    public String describe() {
        String transition = previousStatus == null ? "created as " + newStatus : previousStatus + " -> " + newStatus;
        return "[" + occurredAt + "] order " + orderId + ": " + transition
                + (detail == null || detail.isBlank() ? "" : " (" + detail + ")");
    }
}
