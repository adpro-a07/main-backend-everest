package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

public class UserRequest {
    private final Long id;
    private final String userDescription;

    public UserRequest(Long id, String userDescription) {
        this.id = id;
        this.userDescription = userDescription;
    }

    public Long getId() {
        return id;
    }

    public String getUserDescription() {
        return userDescription;
    }
}