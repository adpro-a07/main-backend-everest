package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.authentication.AuthenticatedUser;
import id.ac.ui.cs.advprog.everest.authentication.CurrentUser;
import id.ac.ui.cs.advprog.everest.common.dto.GenericResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestResponse;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.UserRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserRequestController {
    private final UserRequestService userRequestService;

    public UserRequestController(UserRequestService userRequestService) {
        this.userRequestService = userRequestService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/user-requests")
    public ResponseEntity<?> createUserRequest(
            @Valid @RequestBody CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<ViewUserRequestResponse> response = userRequestService
                .createUserRequest(createAndUpdateUserRequestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user-requests")
    public ResponseEntity<?> getUserRequests(@CurrentUser AuthenticatedUser user) {
        GenericResponse<List<ViewUserRequestResponse>> response = userRequestService.getUserRequests(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user-requests/{requestId}")
    public ResponseEntity<?> getUserRequestById(
            @PathVariable String requestId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<ViewUserRequestResponse> response = userRequestService.getUserRequestById(requestId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/user-requests/{requestId}")
    public ResponseEntity<?> updateUserRequest(
            @PathVariable String requestId,
            @Valid @RequestBody CreateAndUpdateUserRequest createAndUpdateUserRequestDto,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<ViewUserRequestResponse> response = userRequestService
                .updateUserRequest(requestId, createAndUpdateUserRequestDto, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/user-requests/{requestId}")
    public ResponseEntity<?> deleteUserRequest(
            @PathVariable String requestId,
            @CurrentUser AuthenticatedUser user
    ) {
        GenericResponse<Void> response = userRequestService.deleteUserRequest(requestId, user);
        return ResponseEntity.ok(response);
    }
}