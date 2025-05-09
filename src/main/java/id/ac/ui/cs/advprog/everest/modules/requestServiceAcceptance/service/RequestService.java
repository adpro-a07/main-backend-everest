package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.*;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.IncomingRequestRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.StatusLogRepository;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RequestService {
    private final UserRequestRepository userRequestRepository;
    private final IncomingRequestRepository incomingRequestRepository;
    private final StatusLogRepository statusLogRepository;

    private final Map<Long, RequestContext> requestContextCache = new ConcurrentHashMap<>();

    @Autowired
    public RequestService(UserRequestRepository userRequestRepository,
                          IncomingRequestRepository incomingRequestRepository,
                          StatusLogRepository statusLogRepository) {
        this.userRequestRepository = userRequestRepository;
        this.incomingRequestRepository = incomingRequestRepository;
        this.statusLogRepository = statusLogRepository;
    }

    @Transactional
    public UserRequest createUserRequest(UserRequest request) {
        return userRequestRepository.save(request);
    }

    @Transactional
    public IncomingRequest assignToTechnician(Long requestId, Long technicianId) {
        UserRequest userRequest = userRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("User request not found with id: " + requestId));

        IncomingRequest incomingRequest = IncomingRequest.from(userRequest, technicianId);
        return incomingRequestRepository.save(incomingRequest);
    }

    @Transactional(readOnly = true)
    public List<IncomingRequest> getTechnicianRequests(Long technicianId) {
        return incomingRequestRepository.findByTechnicianId(technicianId);
    }

    @Transactional(readOnly = true)
    public List<IncomingRequest> getTechnicianRequestsByStatus(Long technicianId, RequestStatus status) {
        return incomingRequestRepository.findByTechnicianIdAndStatus(technicianId, status);
    }

    @Transactional
    public void processRequestAction(Long requestId, Long technicianId, String action) {
        IncomingRequest request = incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found or not assigned to this technician"));

        RequestContext context = getOrCreateRequestContext(request);

        RequestStatus beforeStatus = context.getCurrentStatus();
        context.processAction(action);
        RequestStatus afterStatus = context.getCurrentStatus();

        if (!beforeStatus.equals(afterStatus)) {
            request = incomingRequestRepository.findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("Request not found after status change"));

            List<StatusLog> logs = context.getStatusLogs();
            if (!logs.isEmpty()) {
                for (StatusLog log : logs) {
                    if (log.getId() == null) {  // Only save new logs
                        statusLogRepository.save(log);
                    }
                }
            }
        }
    }

    private RequestContext getOrCreateRequestContext(TechnicianViewableRequest request) {
        return requestContextCache.computeIfAbsent(request.getRequestId(), k -> {
            RequestContext context = new RequestContext(request);

            List<StatusLog> logs = statusLogRepository.findByRequestIdOrderByTimestampDesc(request.getRequestId());
            for (StatusLog log : logs) {
                context.logStatusChange(log.getOldStatus(), log.getNewStatus());
            }

            return context;
        });
    }

    @Transactional(readOnly = true)
    public RequestContext getRequestContext(Long requestId, Long technicianId) {
        IncomingRequest request = incomingRequestRepository.findByRequestIdAndTechnicianId(requestId, technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found or not assigned to this technician"));

        return getOrCreateRequestContext(request);
    }
}