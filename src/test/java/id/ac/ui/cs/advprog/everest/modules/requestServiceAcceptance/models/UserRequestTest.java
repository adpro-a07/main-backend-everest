package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {
    @Test
    void testUserRequestCreation() {
        UserRequest request = new UserRequest(100L, "Fix washing machine");

        assertEquals(100L, request.getId());
        assertEquals("Fix washing machine", request.getUserDescription());
    }
}