package com.example.crud.aop;


import com.example.crud.common.repository.AbstractJdbcRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

@Repository
public class AuditableTestEntityRepositoryImpl extends AbstractJdbcRepository<AuditableTestEntity, Long> implements AuditableTestEntityRepository {

    public AuditableTestEntityRepositoryImpl(DataSource dataSource, JdbcClient jdbcClient) {
        super(dataSource, jdbcClient);
    }

    @Override
    protected String getTableName() {
        return "test_auditable_entity";
    }

    @Override
    protected String getIdColumnName() {
        return "id";
    }

    @Override
    protected RowMapper<AuditableTestEntity> getRowMapper() {
        return (rs, rowNum) -> {
            AuditableTestEntity entity = new AuditableTestEntity(
                rs.getLong("id"),
                rs.getString("name")
            );
            entity.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            entity.setCreatedBy(rs.getString("created_by"));
            entity.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            entity.setUpdatedBy(rs.getString("updated_by"));
            return entity;
        };
    }

    @Override
    protected Map<String, Object> getUpdateParameters(AuditableTestEntity entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", entity.getName());
        params.put("created_at", entity.getCreatedAt());
        params.put("created_by", entity.getCreatedBy());
        params.put("updated_at", entity.getUpdatedAt());
        params.put("updated_by", entity.getUpdatedBy());
        return params;
    }

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Collections.singleton("name");
    }
}
