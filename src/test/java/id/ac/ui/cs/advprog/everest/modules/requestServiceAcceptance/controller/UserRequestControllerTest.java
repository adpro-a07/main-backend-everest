package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures DB is rolled back after each test
public class UserRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRequestRepository userRequestRepository;

    private CreateAndUpdateUserRequestDto createDto;

    @BeforeEach
    public void setup() {
        createDto = new CreateAndUpdateUserRequestDto();
        createDto.setUserDescription("My laptop won't turn on");
    }

    @Test
    public void testCreateUserRequest() throws Exception {
        mockMvc.perform(post("/api/user-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userDescription", is("My laptop won't turn on")));
    }

    @Test
    public void testGetAllUserRequests() throws Exception {
        // Create two entries
        CreateAndUpdateUserRequestDto dto1 = new CreateAndUpdateUserRequestDto();
        dto1.setUserDescription("First issue");

        CreateAndUpdateUserRequestDto dto2 = new CreateAndUpdateUserRequestDto();
        dto2.setUserDescription("Second issue");

        mockMvc.perform(post("/api/user-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)));

        mockMvc.perform(post("/api/user-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)));

        mockMvc.perform(get("/api/user-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetUserRequestById() throws Exception {
        String response = mockMvc.perform(post("/api/user-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/user-requests/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDescription", is("My laptop won't turn on")));
    }

    @Test
    public void testUpdateUserRequest() throws Exception {
        String response = mockMvc.perform(post("/api/user-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        CreateAndUpdateUserRequestDto updateDto = new CreateAndUpdateUserRequestDto();
        updateDto.setUserDescription("Updated description");

        mockMvc.perform(put("/api/user-requests/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDescription", is("Updated description")));
    }

    @Test
    public void testDeleteUserRequest() throws Exception {
        String response = mockMvc.perform(post("/api/user-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/user-requests/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetUserRequestByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/user-requests/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUserRequestNotFound() throws Exception {
        CreateAndUpdateUserRequestDto updateDto = new CreateAndUpdateUserRequestDto();
        updateDto.setUserDescription("Updated");

        mockMvc.perform(put("/api/user-requests/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUserRequestNotFound() throws Exception {
        mockMvc.perform(delete("/api/user-requests/9999"))
                .andExpect(status().isNotFound());
    }
}
