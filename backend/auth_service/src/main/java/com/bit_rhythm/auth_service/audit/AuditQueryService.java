package com.bit_rhythm.auth_service.audit;

import com.bit_rhythm.auth_service.audit.dto.AuditLogFilter;
import com.bit_rhythm.auth_service.audit.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditLogRepository repository;

    public AuditQueryService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public Page<AuditLogResponse> search(AuditLogFilter filter, Pageable pageable) {
        return repository
                .findAll(AuditLogSpecifications.from(filter), pageable)
                .map(AuditLogResponse::from);
    }
}
