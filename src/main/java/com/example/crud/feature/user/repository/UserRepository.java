
package com.example.crud.feature.user.repository;

import com.example.crud.common.repository.AbstractJdbcRepository;
import com.example.crud.feature.role.model.Role;
import com.example.crud.feature.user.model.User;
import com.example.crud.util.TimerUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepository extends AbstractJdbcRepository<User, Long> implements UserDetailsService {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        // Ambil dari alias query: user_name dan user_password
        user.setUsername(rs.getString("user_username"));
        user.setPassword(rs.getString("user_password"));
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
        params.put(USERNAME, user.getUsername());
        params.put(PASSWORD, user.getPassword());
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
        return Set.of("id", USERNAME, PASSWORD);
    }

    @Override
    public Optional<User> findById(Long id) {
        return TimerUtil.time("findById", () -> {
            String sql = """
                        SELECT
                            u.id as user_id, u.username as user_username, u.password as user_password,
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
            Map<String, Object> actualFilters = processFilters(filters);
            String countSql = buildCountSql(actualFilters);
            logQuery(countSql, actualFilters);
            Long totalElements = jdbcClient.sql(countSql)
                    .params(actualFilters)
                    .query(Long.class)
                    .single();

            String dataSql = buildDataSql(actualFilters, pageable);
            Map<String, Object> queryParams = new LinkedHashMap<>(actualFilters);
            queryParams.put("limit", pageable.getPageSize());
            queryParams.put("offset", pageable.getOffset());

            logQuery(dataSql, queryParams);
            List<User> content = jdbcClient.sql(dataSql)
                    .params(queryParams)
                    .query(getRowMapper())
                    .list();

            return new PageImpl<>(content, pageable, totalElements);
        });
    }

    private Map<String, Object> processFilters(Map<String, Object> filters) {
        Map<String, Object> actualFilters = new LinkedHashMap<>();
        if (filters != null && !filters.isEmpty()) {
            filters.forEach((k, v) -> {
                if (k.equals(USERNAME)) {
                    actualFilters.put(USERNAME, v);
                } else if (k.equals("role")) {
                    if (v instanceof com.example.crud.feature.role.model.Role roleObj && roleObj.getId() != null) {
                        actualFilters.put("role_id", roleObj.getId());
                    } else if (v instanceof Number) {
                        actualFilters.put("role_id", v);
                    }
                } else {
                    actualFilters.put(k, v);
                }
            });
        }
        return actualFilters;
    }

    private String buildCountSql(Map<String, Object> actualFilters) {
        StringBuilder countSql = new StringBuilder("SELECT count(*) FROM %s u".formatted(getTableName()));
        if (!actualFilters.isEmpty()) {
            String whereClause = buildWhereClause(actualFilters, "u");
            countSql.append(" WHERE ").append(whereClause);
        }
        return countSql.toString();
    }

    private String buildDataSql(Map<String, Object> actualFilters, Pageable pageable) {
        StringBuilder dataSql = new StringBuilder(("""
                    SELECT
                        u.id as user_id, u.username as user_username, u.password as user_password,
                        u.created_at as user_created_at, u.created_by as user_created_by,
                        u.updated_at as user_updated_at, u.updated_by as user_updated_by,
                        r.id as role_id, r.name as role_name, r.description as role_description
                    FROM users u
                    LEFT JOIN roles r ON u.role_id = r.id
                """).stripIndent().trim());

        if (!actualFilters.isEmpty()) {
            String whereClause = buildWhereClause(actualFilters, "u");
            dataSql.append(" WHERE ").append(whereClause);
        }

        String sortClause = buildSortClause(pageable.getSort(), "u");
        if (!sortClause.isEmpty()) {
            dataSql.append(" ORDER BY ").append(sortClause);
        }

        dataSql.append(" LIMIT :limit OFFSET :offset");
        return dataSql.toString();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.findAll(PageRequest.of(0, 1), Map.of(USERNAME, username))
                .getContent().stream().findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singleton(authority))
                .build();
    }
}