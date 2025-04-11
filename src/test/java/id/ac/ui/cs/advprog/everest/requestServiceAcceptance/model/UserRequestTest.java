package id.ac.ui.cs.advprog.everest.requestServiceAcceptance.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {
    @Test
    void testUserRequestCreation() {
        // Arrange & Act
        UserRequest request = new UserRequest(100L, "Fix washing machine");

        // Assert
        assertEquals(100L, request.getId());
        assertEquals("Fix washing machine", request.getUserDescription());
    }
}