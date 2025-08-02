package com.example.crud.feature.user.repository;

import com.example.crud.common.repository.AbstractJdbcRepository;
import com.example.crud.feature.user.model.User;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class UserRepository extends AbstractJdbcRepository<User, Long> {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        // Mapping kolom audit
        user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        user.setCreatedBy(rs.getString("created_by"));
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        user.setUpdatedBy(rs.getString("updated_by"));
        return user;
    };
    
    public UserRepository(DataSource dataSource, JdbcClient jdbcClient) {
        super(dataSource, jdbcClient);
    }

    @Override
    protected String getTableName() {
        return "users";
    }
    
    @Override
    protected String getIdColumnName() {
        return "id";
    }

    @Override
    protected RowMapper<User> getRowMapper() {
        return USER_ROW_MAPPER;
    }

    @Override
    protected Map<String, Object> getUpdateParameters(User user) {
        // Gunakan LinkedHashMap untuk menjaga urutan, penting untuk SimpleJdbcInsert
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", user.getName());
        params.put("email", user.getEmail());
        // Menambahkan parameter audit
        params.put("created_at", user.getCreatedAt());
        params.put("created_by", user.getCreatedBy());
        params.put("updated_at", user.getUpdatedAt());
        params.put("updated_by", user.getUpdatedBy());
        return params;
    }

    @Override
    protected Set<String> getAllowedSortColumns() {
        // Daftarkan semua kolom yang boleh digunakan untuk sorting
        return Set.of("id", "name", "email");
    }
}