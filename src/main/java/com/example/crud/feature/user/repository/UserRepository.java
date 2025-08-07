package com.example.crud.feature.user.repository;


import com.example.crud.common.repository.AbstractJdbcRepository;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.user.model.User;
import com.example.crud.util.TimerUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepository extends AbstractJdbcRepository<User, Long> {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));        
        user.setPassword(rs.getString("password"));
        // Mapping kolom audit
        user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        user.setCreatedBy(rs.getString("created_by"));
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        user.setUpdatedBy(rs.getString("updated_by"));

        // Jika ada role yang ter-join, buat objek Role
        if (rs.getObject("role_id") != null) {
            Role role = new Role();
            role.setId(rs.getLong("role_id"));
            role.setName(rs.getString("role_name"));
            role.setDescription(rs.getString("role_description"));
            user.setRole(role);
        }
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
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());
        if (user.getRole() != null) {
            params.put("role_id", user.getRole().getId());
        }
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
        return Set.of("id", "username", "password");
    }

    @Override
    public Optional<User> findById(Long id) {
        return TimerUtil.time("findById", () -> {
            String sql = """
                SELECT
                    u.id as user_id, u.username as user_name, u.password as user_password,
                    u.created_at as user_created_at, u.created_by as user_created_by,
                    u.updated_at as user_updated_at, u.updated_by as user_updated_by,
                    r.id as role_id, r.name as role_name, r.description as role_description
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
                WHERE u.id = :id
            """.stripIndent().trim();
            Map<String, Object> params = Map.of("id", id);
            logQuery(sql, params);
            return jdbcClient.sql(sql)
                    .param("id", id)
                    .query(getRowMapper())
                    .optional();
        });
    }

    @Override
    public Page<User> findAll(Pageable pageable, Map<String, Object> filters) {
        return TimerUtil.time("findAll", () -> {
            // --- Count Query (No change needed here) ---
            StringBuilder countSql = new StringBuilder("SELECT count(*) FROM %s u".formatted(getTableName()));
            if (filters != null && !filters.isEmpty()) {
                String whereClause = buildWhereClause(filters, "u"); // Pass alias 'u'
                countSql.append(" WHERE ").append(whereClause);
            }
            logQuery(countSql.toString(), filters);
            Long totalElements = jdbcClient.sql(countSql.toString())
                                        .params(filters)
                                        .query(Long.class)
                                        .single();

            // Gunakan query dengan JOIN dan alias
            StringBuilder dataSql = new StringBuilder(("""
                SELECT
                    u.id as user_id, u.username as user_name, u.password as user_password,
                    u.created_at as user_created_at, u.created_by as user_created_by,
                    u.updated_at as user_updated_at, u.updated_by as user_updated_by,
                    r.id as role_id, r.name as role_name, r.description as role_description
                FROM users u
                LEFT JOIN roles r ON u.role_id = r.id
            """).stripIndent().trim());

            if (filters != null && !filters.isEmpty()) {
                String whereClause = buildWhereClause(filters, "u"); // Pass alias 'u'
                dataSql.append(" WHERE ").append(whereClause);
            }

            String sortClause = buildSortClause(pageable.getSort(), "u"); // Pass alias 'u'
            if (!sortClause.isEmpty()) {
                dataSql.append(" ORDER BY ").append(sortClause);
            }

            dataSql.append(" LIMIT :limit OFFSET :offset");

            Map<String, Object> queryParams = new LinkedHashMap<>(filters != null ? filters : Map.of());
            queryParams.put("limit", pageable.getPageSize());
            queryParams.put("offset", pageable.getOffset());

            logQuery(dataSql.toString(), queryParams);
            List<User> content = jdbcClient.sql(dataSql.toString())
                                        .params(queryParams)
                                        .query(getRowMapper())
                                        .list();

            return new PageImpl<>(content, pageable, totalElements);
        });
    }
}