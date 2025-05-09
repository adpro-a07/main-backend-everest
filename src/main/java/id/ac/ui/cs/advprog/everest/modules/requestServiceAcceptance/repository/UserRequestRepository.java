package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRequestRepository {
    private final Map<Long, UserRequest> userRequests = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public UserRequest save(UserRequest userRequest) {
        if (userRequest.getId() == null) {
            Long newId = idCounter.incrementAndGet();
            UserRequest newRequest = new UserRequest(newId, userRequest.getUserDescription());
            userRequests.put(newId, newRequest);
            return newRequest;
        } else {
            userRequests.put(userRequest.getId(), userRequest);
            return userRequest;
        }
    }

    public Optional<UserRequest> findById(Long id) {
        return Optional.ofNullable(userRequests.get(id));
    }

    public List<UserRequest> findAll() {
        return new ArrayList<>(userRequests.values());
    }

    public boolean deleteById(Long id) {
        return userRequests.remove(id) != null;
    }
}