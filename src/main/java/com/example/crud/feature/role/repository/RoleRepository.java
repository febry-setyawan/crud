package com.example.crud.feature.role.repository;

import com.example.crud.common.repository.AbstractJdbcRepository;
import com.example.crud.feature.role.model.Role;
import static com.example.crud.feature.role.RoleConstants.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Repository

public class RoleRepository extends AbstractJdbcRepository<Role, Long> {

    private static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> {
        Role role = new Role();
        role.setId(rs.getLong(ID));
        role.setName(rs.getString(NAME));
        role.setDescription(rs.getString(DESCRIPTION));
        return role;
    };

    public RoleRepository(DataSource dataSource, JdbcClient jdbcClient) {
        super(dataSource, jdbcClient);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getIdColumnName() {
        return ID;
    }

    @Override
    protected RowMapper<Role> getRowMapper() {
        return ROLE_ROW_MAPPER;
    }

    @Override
    protected Map<String, Object> getUpdateParameters(Role role) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(NAME, role.getName());
        params.put(DESCRIPTION, role.getDescription());
        return params;
    }

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of(ID, NAME, DESCRIPTION);
    }
}