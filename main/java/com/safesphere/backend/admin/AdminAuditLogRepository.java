package com.safesphere.backend.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
    List<AdminAuditLog> findByAdminUsernameOrderByTimestampDesc(String adminUsername);
    List<AdminAuditLog> findByTargetEntityAndTargetId(String targetEntity, Long targetId);
    List<AdminAuditLog> findAllByOrderByTimestampDesc();
}
