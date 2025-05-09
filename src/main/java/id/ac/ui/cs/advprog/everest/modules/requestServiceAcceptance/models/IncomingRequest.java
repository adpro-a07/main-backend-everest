package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technician_requests")
@Getter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class IncomingRequest implements TechnicianViewableRequest {
    @Id
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

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