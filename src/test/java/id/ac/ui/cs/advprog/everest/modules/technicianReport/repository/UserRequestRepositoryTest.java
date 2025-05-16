package id.ac.ui.cs.advprog.everest.modules.technicianReport.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.model.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRequestRepository userRequestRepository;

    @Test
    void findByUserIdAndUserDescription_ShouldReturnRequest_WhenExists() {
        UUID userId = UUID.randomUUID();
        String description = "Fix my laptop screen";

        UserRequest request = new UserRequest(userId, description);
        entityManager.persistAndFlush(request);

        Optional<UserRequest> found = userRequestRepository.findByUserIdAndUserDescription(userId, description);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getUserDescription()).isEqualTo(description);
    }

    @Test
    void findByUserIdAndUserDescription_ShouldReturnEmpty_WhenNotExists() {
        Optional<UserRequest> found = userRequestRepository.findByUserIdAndUserDescription(
                UUID.randomUUID(), "Nonexistent request");

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnAllUserRequests() {
        UUID userId = UUID.randomUUID();

        UserRequest request1 = new UserRequest(userId, "Fix washing machine");
        entityManager.persistAndFlush(request1);

        UserRequest request2 = new UserRequest(userId, "Fix refrigerator");
        entityManager.persistAndFlush(request2);

        UserRequest request3 = new UserRequest(UUID.randomUUID(), "Fix microwave");
        entityManager.persistAndFlush(request3);

        List<UserRequest> requests = userRequestRepository.findByUserId(userId);

        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(UserRequest::getUserId)
                .containsOnly(userId);
        assertThat(requests).extracting(UserRequest::getUserDescription)
                .containsExactlyInAnyOrder("Fix washing machine", "Fix refrigerator");
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenUserHasNoRequests() {
        List<UserRequest> requests = userRequestRepository.findByUserId(UUID.randomUUID());

        assertThat(requests).isEmpty();
    }

    @Test
    void save_ShouldGenerateRequestId_WhenNotProvided() {
        UUID userId = UUID.randomUUID();
        UserRequest request = new UserRequest();
        request.setUserId(userId);
        request.setUserDescription("Fix ceiling fan");

        UserRequest savedRequest = userRequestRepository.save(request);

        assertThat(savedRequest.getRequestId()).isNotNull();
    }

    @Test
    void save_ShouldNotOverrideRequestId_WhenAlreadyProvided() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserRequest request = new UserRequest(userId, "Install new sink");
        request.setRequestId(requestId);

        UserRequest savedRequest = userRequestRepository.save(request);

        assertThat(savedRequest.getRequestId()).isEqualTo(requestId);
    }

    @Test
    void findById_ShouldReturnRequest_WhenExists() {
        UserRequest request = new UserRequest(UUID.randomUUID(), "Repair door lock");
        UserRequest savedRequest = entityManager.persistAndFlush(request);
        UUID requestId = savedRequest.getRequestId();

        Optional<UserRequest> found = userRequestRepository.findById(requestId);

        assertThat(found).isPresent();
        assertThat(found.get().getRequestId()).isEqualTo(requestId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<UserRequest> found = userRequestRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndUserDescription_ShouldThrowException_WhenNullParameters() {
        Optional<UserRequest> result = userRequestRepository.findByUserIdAndUserDescription(null, "Description");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveRequest() {
        UserRequest request = new UserRequest(UUID.randomUUID(), "Fix lighting");
        UserRequest savedRequest = entityManager.persistAndFlush(request);
        UUID requestId = savedRequest.getRequestId();

        userRequestRepository.deleteById(requestId);
        userRequestRepository.flush(); // Ensure the delete is flushed to the DB

        Optional<UserRequest> found = userRequestRepository.findById(requestId);
        assertThat(found).isEmpty();
    }
}