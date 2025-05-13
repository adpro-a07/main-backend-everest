package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RequestContext {
    private RequestState currentState;
    private final TechnicianViewableRequest request;
    private final List<StatusLog> statusLogs = new ArrayList<>();

    public RequestContext(TechnicianViewableRequest request) {
        this.request = request;
        this.currentState = new PendingState();
    }

    public void processAction(String action) {
        currentState = currentState.processAction(action, this);
    }

    public RequestStatus getCurrentStatus() {
        return currentState.getStatus();
    }

    public TechnicianViewableRequest getRequest() {
        return request;
    }

    public void logStatusChange(RequestStatus oldStatus, RequestStatus newStatus) {
        statusLogs.add(new StatusLog(UUID.fromString(request.getRequestId()), oldStatus,
                newStatus, UUID.fromString(request.getTechnicianId())));
    }

    public StatusLog getLastStatusLog() {
        if (statusLogs.isEmpty()) {
            return null;
        }
        return statusLogs.get(statusLogs.size() - 1);
    }

    public List<StatusLog> getStatusLogs() {
        return Collections.unmodifiableList(statusLogs);
    }
}