package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.controller;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service.UserRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-requests")
public class UserRequestController {

    private final UserRequestService userRequestService;

    @Autowired
    public UserRequestController(UserRequestService userRequestService) {
        this.userRequestService = userRequestService;
    }

    @PostMapping
    public ResponseEntity<ViewUserRequestDto> createUserRequest(
            @Valid @RequestBody CreateAndUpdateUserRequestDto requestDto) {
        ViewUserRequestDto createdRequest = userRequestService.createUserRequest(requestDto);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ViewUserRequestDto>> getAllUserRequests() {
        List<ViewUserRequestDto> userRequests = userRequestService.getAllUserRequests();
        return new ResponseEntity<>(userRequests, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViewUserRequestDto> getUserRequestById(@PathVariable Long id) {
        return userRequestService.getUserRequestById(id)
                .map(requestDto -> new ResponseEntity<>(requestDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViewUserRequestDto> updateUserRequest(
            @PathVariable Long id,
            @Valid @RequestBody CreateAndUpdateUserRequestDto requestDto) {
        return userRequestService.updateUserRequest(id, requestDto)
                .map(updatedDto -> new ResponseEntity<>(updatedDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRequest(@PathVariable Long id) {
        boolean deleted = userRequestService.deleteUserRequest(id);
        return deleted ?
                new ResponseEntity<>(HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}