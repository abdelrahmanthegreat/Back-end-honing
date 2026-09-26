package masr;

public final class AuditLogger implements OrderEventListener {

    private final AuditLog auditLog;

    public AuditLogger(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        auditLog.record("platform", event);
    }

    @Override
    public String name() {
        return "AuditLogger";
    }

    public AuditLog log() {
        return auditLog;
    }
}
