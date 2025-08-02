package com.example.crud.feature.role.repository;

import com.example.crud.common.repository.AbstractJdbcRepository;
import com.example.crud.feature.role.model.Role;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Repository
// Hapus "implements RoleRepository" dari sini
public class RoleRepository extends AbstractJdbcRepository<Role, Long> {

    private static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        return role;
    };

    public RoleRepository(DataSource dataSource, JdbcClient jdbcClient) {
        super(dataSource, jdbcClient);
    }

    @Override
    protected String getTableName() {
        return "roles";
    }

    @Override
    protected String getIdColumnName() {
        return "id";
    }

    @Override
    protected RowMapper<Role> getRowMapper() {
        return ROLE_ROW_MAPPER;
    }

    @Override
    protected Map<String, Object> getUpdateParameters(Role role) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", role.getName());
        params.put("description", role.getDescription());
        return params;
    }

    @Override
    protected Set<String> getAllowedSortColumns() {
        return Set.of("id", "name", "description");
    }
}