package ma.nexotek.HireCraft.enums;

public enum  EventStatus {
    COMPLETED("Terminé"),
    SCHEDULED("Planifié"),
    PENDING("En attente"),
    CANCELLED("Annulé");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}