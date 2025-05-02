package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models;

public class IncomingRequest implements TechnicianViewableRequest {
    private final Long requestId;
    private final Long technicianId;
    private final String description;
    private final RequestStatus status;

    public IncomingRequest(Long requestId, Long technicianId, String description, RequestStatus status) {
        this.requestId = requestId;
        this.technicianId = technicianId;
        this.description = description;
        this.status = status;
    }

    public static IncomingRequest from(UserRequest userRequest, Long technicianId) {
        return new IncomingRequest(
                userRequest.getId(),
                technicianId,
                userRequest.getUserDescription(),
                RequestStatus.PENDING
        );
    }

    @Override
    public Long getRequestId() {
        return requestId;
    }

    @Override
    public Long getTechnicianId() {
        return technicianId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public RequestStatus getStatus() {
        return status;
    }

    public IncomingRequest withDescription(String newDescription) {
        throw new UnsupportedOperationException("IncomingRequest is immutable");
    }
}