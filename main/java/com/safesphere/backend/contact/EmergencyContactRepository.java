package com.safesphere.backend.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findAllByOwnerEmailIgnoreCaseOrderByIdDesc(String email);
    Optional<EmergencyContact> findByIdAndOwnerEmailIgnoreCase(Long id, String email);
}
