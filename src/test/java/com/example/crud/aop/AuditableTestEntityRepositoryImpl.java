package com.example.crud.aop;

import com.example.crud.common.repository.AbstractJdbcRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AuditableTestEntityRepositoryImpl extends AbstractJdbcRepository<AuditableTestEntity, Long> implements AuditableTestEntityRepository {

    public AuditableTestEntityRepositoryImpl(JdbcClient jdbcClient) {
        super(jdbcClient, "test_auditable_entity", "id");
    }

    // The save and update methods are inherited from the abstract class
    // and will be advised by the AuditTrailAspect.
}
