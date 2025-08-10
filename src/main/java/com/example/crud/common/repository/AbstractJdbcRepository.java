package com.example.crud.common.repository;

import com.example.crud.common.model.BaseEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import javax.sql.DataSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.crud.util.TimerUtil;

public abstract class AbstractJdbcRepository<T extends BaseEntity<ID>, ID> implements GenericRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcRepository.class);

    protected final JdbcClient jdbcClient;
    protected final SimpleJdbcInsert simpleJdbcInsert;

    // Metode abstrak yang HARUS diimplementasikan oleh kelas turunan
    protected abstract String getTableName();
    protected abstract String getIdColumnName();
    protected abstract RowMapper<T> getRowMapper();
    protected abstract Map<String, Object> getUpdateParameters(T entity);
    protected abstract Set<String> getAllowedSortColumns();

    protected AbstractJdbcRepository(DataSource dataSource, JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName(getTableName())
                .usingGeneratedKeyColumns(getIdColumnName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T save(T entity) {
        return TimerUtil.time("save", () -> {
            Map<String, Object> params = getUpdateParameters(entity);
            Number newId = simpleJdbcInsert.executeAndReturnKey(params);
            return findById((ID) newId)
                    .orElseThrow(() -> new IllegalStateException("Could not find saved entity with id: " + newId));
        });
    }

    @Override
    public Optional<T> findById(ID id) {
        return TimerUtil.time("findById", () -> {
            String sql = "SELECT * FROM %s WHERE %s = :id".formatted(getTableName(), getIdColumnName());
            Map<String, Object> params = Map.of("id",id);
            logQuery(sql, params);
            return jdbcClient.sql(sql)
                    .param("id", id)
                    .query(getRowMapper())
                    .optional();
        });
    }

    @Override
    public Page<T> findAll(Pageable pageable, Map<String, Object> filters) {
        return TimerUtil.time("findAll", () -> {
            // === QUERY 1: Menghitung total elemen dengan filter yang sama ===
            StringBuilder countSql = new StringBuilder("SELECT count(*) FROM %s".formatted(getTableName()));
            Map<String, Object> safeFilters = (filters != null) ? filters : Map.of();
            if (!safeFilters.isEmpty()) {
                String whereClause = buildWhereClause(safeFilters);
                countSql.append(" WHERE ").append(whereClause);
            }

            logQuery(countSql.toString(), safeFilters);
            Long totalElements = jdbcClient.sql(countSql.toString())
                                        .params(safeFilters)
                                        .query(Long.class)
                                        .single();

            // === QUERY 2: Mengambil data untuk halaman saat ini ===
            StringBuilder dataSql = new StringBuilder("SELECT * FROM %s".formatted(getTableName()));
            if (!safeFilters.isEmpty()) {
                String whereClause = buildWhereClause(safeFilters);
                dataSql.append(" WHERE ").append(whereClause);
            }

            // Tambahkan sorting dari Pageable
            String sortClause = buildSortClause(pageable.getSort());
            if (!sortClause.isEmpty()) {
                dataSql.append(" ORDER BY ").append(sortClause);
            }

            // Tambahkan pagination
            dataSql.append(" LIMIT :limit OFFSET :offset");

            Map<String, Object> queryParams = new LinkedHashMap<>(safeFilters);
            queryParams.put("limit", pageable.getPageSize());
            queryParams.put("offset", pageable.getOffset());

            logQuery(dataSql.toString(), queryParams);
            List<T> content = jdbcClient.sql(dataSql.toString())
                                        .params(queryParams)
                                        .query(getRowMapper())
                                        .list();

            // Gabungkan hasil menjadi objek Page
            return new PageImpl<>(content, pageable, totalElements);
        });
    }

    // --- Helper Methods ---
    protected String buildWhereClause(Map<String, Object> filters) {
        return filters.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    // Jika nilai adalah String dan mengandung '%', gunakan LIKE. Jika tidak, gunakan =.
                    if (value instanceof String && value.toString().contains("%")) {
                        return "%s LIKE :%s".formatted(key, key);
                    } else {
                        return "%s = :%s".formatted(key, key);
                    }
                })
                .collect(Collectors.joining(" AND "));
    }

    protected String buildWhereClause(Map<String, Object> filters, String alias) {
        return filters.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    // Jika nilai adalah String dan mengandung '%', gunakan LIKE. Jika tidak, gunakan =.
                    if (value instanceof String && value.toString().contains("%")) {
                        return "%s.%s LIKE :%s".formatted(alias, key, key);
                    } else {
                        return "%s.%s = :%s".formatted(alias, key, key);
                    }
                })
                .collect(Collectors.joining(" AND "));
    }

    protected String buildSortClause(Sort sort) {
        return sort.stream()
                .map(order -> {
                    // Validasi kolom sort dengan whitelist untuk keamanan
                    if (getAllowedSortColumns().contains(order.getProperty())) {
                        return order.getProperty() + " " + (order.isAscending() ? "ASC" : "DESC");
                    }
                    return null;
                })
                .filter(s -> s != null)
                .collect(Collectors.joining(", "));
    }

    protected String buildSortClause(Sort sort, String alias) {
        return sort.stream()
                .map(order -> {
                    if (getAllowedSortColumns().contains(order.getProperty())) {
                        // Gunakan alias yang diberikan
                        return alias + "." + order.getProperty() + " " + (order.isAscending() ? "ASC" : "DESC");
                    }
                    return null;
                })
                .filter(s -> s != null)
                .collect(Collectors.joining(", "));
    }


    @Override
    public int update(T entity) {
        return TimerUtil.time("update", () -> {
            // Membuat daftar kolom untuk di-update (e.g., "name = :name, email = :email")
            String setClause = String.join(", ",
                    getUpdateParameters(entity).keySet().stream()
                            .map(key -> key + " = :" + key)
                            .toList());

            String sql = "UPDATE %s SET %s WHERE %s = :id"
                    .formatted(getTableName(), setClause, getIdColumnName());
            
            // Menambahkan ID ke map parameter
            Map<String, Object> params = getUpdateParameters(entity);
            params.put("id", entity.getId());

            logQuery(sql, params);
            return jdbcClient.sql(sql)
                    .params(params)
                    .update();
        });
    }

    @Override
    public int deleteById(ID id) {
        return TimerUtil.time("deleteById", () -> {
            String sql = "DELETE FROM %s WHERE %s = :id".formatted(getTableName(), getIdColumnName());
            Map<String, Object> params = Map.of("id", id);
            logQuery(sql, params);
            return jdbcClient.sql(sql)
                    .param("id", id)
                    .update();
        });
    }

    protected void logQuery(String sql, Map<String, Object> params) {
        log.trace("Execute Query : {}", sql);
        if (params != null && !params.isEmpty()) {
            log.debug("Parameter Query : {}", params);
        }
    }
}