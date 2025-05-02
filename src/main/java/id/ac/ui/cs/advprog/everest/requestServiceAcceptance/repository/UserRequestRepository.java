package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.requestServiceAcceptance.models.UserRequest;
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
            // Create new request with generated ID
            Long newId = idCounter.incrementAndGet();
            UserRequest newRequest = new UserRequest(newId, userRequest.getUserDescription());
            userRequests.put(newId, newRequest);
            return newRequest;
        } else if (userRequests.containsKey(userRequest.getId())) {
            throw new IllegalArgumentException("UserRequest with this ID already exists");
        }else{
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