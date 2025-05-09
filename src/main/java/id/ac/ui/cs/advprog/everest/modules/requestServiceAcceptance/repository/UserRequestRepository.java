package id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.repository;

import id.ac.ui.cs.advprog.everest.modules.requestServiceAcceptance.models.UserRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, Long> { }