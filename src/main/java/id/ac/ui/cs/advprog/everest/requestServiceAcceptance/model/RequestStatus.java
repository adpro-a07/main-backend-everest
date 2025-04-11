package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

public enum RequestStatus {
    PENDING("Menunggu"),
    REPORTED("Dilaporkan"),
    ESTIMATED("Estimasi Dibuat"),
    ACCEPTED("Disetujui"),
    REJECTED("Ditolak"),
    IN_PROGRESS("Diproses");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}