package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

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
        // Arrange
        UUID userId = UUID.randomUUID();
        String description = "Fix my laptop screen";

        UserRequest request = new UserRequest(userId, description);
        entityManager.persistAndFlush(request);

        // Act
        Optional<UserRequest> found = userRequestRepository.findByUserIdAndUserDescription(userId, description);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getUserDescription()).isEqualTo(description);
    }

    @Test
    void findByUserIdAndUserDescription_ShouldReturnEmpty_WhenNotExists() {
        // Act
        Optional<UserRequest> found = userRequestRepository.findByUserIdAndUserDescription(
                UUID.randomUUID(), "Nonexistent request");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnAllUserRequests() {
        // Arrange
        UUID userId = UUID.randomUUID();

        UserRequest request1 = new UserRequest(userId, "Fix washing machine");
        entityManager.persistAndFlush(request1);

        UserRequest request2 = new UserRequest(userId, "Fix refrigerator");
        entityManager.persistAndFlush(request2);

        // Different user
        UserRequest request3 = new UserRequest(UUID.randomUUID(), "Fix microwave");
        entityManager.persistAndFlush(request3);

        // Act
        List<UserRequest> requests = userRequestRepository.findByUserId(userId);

        // Assert
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(UserRequest::getUserId)
                .containsOnly(userId);
        assertThat(requests).extracting(UserRequest::getUserDescription)
                .containsExactlyInAnyOrder("Fix washing machine", "Fix refrigerator");
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenUserHasNoRequests() {
        // Act
        List<UserRequest> requests = userRequestRepository.findByUserId(UUID.randomUUID());

        // Assert
        assertThat(requests).isEmpty();
    }

    @Test
    void save_ShouldGenerateRequestId_WhenNotProvided() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserRequest request = new UserRequest();
        request.setUserId(userId);
        request.setUserDescription("Fix ceiling fan");

        // Act
        UserRequest savedRequest = userRequestRepository.save(request);

        // Assert
        assertThat(savedRequest.getRequestId()).isNotNull();
    }

    @Test
    void save_ShouldNotOverrideRequestId_WhenAlreadyProvided() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserRequest request = new UserRequest(userId, "Install new sink");
        request.setRequestId(requestId);

        // Act
        UserRequest savedRequest = userRequestRepository.save(request);

        // Assert
        assertThat(savedRequest.getRequestId()).isEqualTo(requestId);
    }

    @Test
    void findById_ShouldReturnRequest_WhenExists() {
        // Arrange
        UserRequest request = new UserRequest(UUID.randomUUID(), "Repair door lock");
        UserRequest savedRequest = entityManager.persistAndFlush(request);
        UUID requestId = savedRequest.getRequestId();

        // Act
        Optional<UserRequest> found = userRequestRepository.findById(requestId);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getRequestId()).isEqualTo(requestId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Act
        Optional<UserRequest> found = userRequestRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndUserDescription_ShouldThrowException_WhenNullParameters() {
        Optional<UserRequest> result = userRequestRepository.findByUserIdAndUserDescription(null, "Description");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveRequest() {
        // Arrange
        UserRequest request = new UserRequest(UUID.randomUUID(), "Fix lighting");
        UserRequest savedRequest = entityManager.persistAndFlush(request);
        UUID requestId = savedRequest.getRequestId();

        // Act
        userRequestRepository.deleteById(requestId);
        userRequestRepository.flush(); // Ensure the delete is flushed to the DB

        // Assert
        Optional<UserRequest> found = userRequestRepository.findById(requestId);
        assertThat(found).isEmpty();
    }
}