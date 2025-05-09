package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.service;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.CreateAndUpdateUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.dto.ViewUserRequestDto;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository.UserRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRequestServiceImpl implements UserRequestService {

    private final UserRequestRepository userRequestRepository;

    @Autowired
    public UserRequestServiceImpl(UserRequestRepository userRequestRepository) {
        this.userRequestRepository = userRequestRepository;
    }

    @Override
    @Transactional
    public ViewUserRequestDto createUserRequest(CreateAndUpdateUserRequestDto requestDto) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserDescription(requestDto.getUserDescription());

        UserRequest savedRequest = userRequestRepository.save(userRequest);

        return mapToViewDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewUserRequestDto> getAllUserRequests() {
        return userRequestRepository.findAll().stream()
                .map(this::mapToViewDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ViewUserRequestDto> getUserRequestById(Long id) {
        return userRequestRepository.findById(id)
                .map(this::mapToViewDto);
    }

    @Override
    @Transactional
    public Optional<ViewUserRequestDto> updateUserRequest(Long id, CreateAndUpdateUserRequestDto requestDto) {
        return userRequestRepository.findById(id)
                .map(userRequest -> {
                    userRequest.setUserDescription(requestDto.getUserDescription());
                    UserRequest updatedRequest = userRequestRepository.save(userRequest);
                    return mapToViewDto(updatedRequest);
                });
    }

    @Override
    @Transactional
    public boolean deleteUserRequest(Long id) {
        if (userRequestRepository.existsById(id)) {
            userRequestRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ViewUserRequestDto mapToViewDto(UserRequest userRequest) {
        return ViewUserRequestDto.builder()
                .id(userRequest.getId())
                .userDescription(userRequest.getUserDescription())
                .createdAt(LocalDateTime.now())  // This would normally come from an @CreatedDate field
                .updatedAt(LocalDateTime.now())  // This would normally come from an @LastModifiedDate field
                .build();
    }
}