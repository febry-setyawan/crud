package com.example.crud.common.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AbstractJdbcRepositoryTest {
    private DummyRepository repository;
    private JdbcClient jdbcClient;
    private DataSource dataSource;
    private SimpleJdbcInsert simpleJdbcInsert;

    @BeforeEach
    void setUp() {
        jdbcClient = mock(JdbcClient.class, RETURNS_DEEP_STUBS);
        dataSource = mock(DataSource.class);
        simpleJdbcInsert = mock(SimpleJdbcInsert.class);
        repository = new DummyRepository(dataSource, jdbcClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void findAll_withWildcardAndSort_shouldCoverAllBranches() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.asc("name")));
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("name", "%admin%");
        filters.put("age", 30);

        // Mock count query
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(Class.class)).single()).thenReturn(1L);
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(RowMapper.class)).list())
                .thenReturn(List.of(new DummyEntity(1L, "admin")));

        Page<DummyEntity> page = repository.findAll(pageable, filters);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void buildWhereClause_shouldUseLikeAndEquals() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("name", "%admin%");
        filters.put("age", 30);
        String where = repository.buildWhereClause(filters);
        assertThat(where).contains("name LIKE :name").contains("age = :age");
    }

    @Test
    void buildWhereClause_withAlias_shouldUseLikeAndEquals() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("name", "%admin%");
        filters.put("age", 30);
        String where = repository.buildWhereClause(filters, "u");
        assertThat(where).contains("u.name LIKE :name").contains("u.age = :age");
    }

    @Test
    void buildSortClause_shouldWhitelistColumns() {
        Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("age"));
        String clause = repository.buildSortClause(sort);
        assertThat(clause)
                .contains("name ASC")
                // 'age' not in allowed, so not present
                .doesNotContain("age DESC");
    }

    @Test
    void buildSortClause_withAlias_shouldWhitelistColumns() {
        Sort sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("age"));
        String clause = repository.buildSortClause(sort, "u");
        assertThat(clause)
                .contains("u.name ASC")
                .doesNotContain("u.age DESC");
    }

    @Test
    void update_shouldCallJdbcUpdate() {
        DummyEntity entity = new DummyEntity(1L, "admin");
        when(simpleJdbcInsert.executeAndReturnKey(anyMap())).thenReturn(1L);
        when(jdbcClient.sql(anyString()).param(anyString(), any()).query(ArgumentMatchers.<RowMapper<DummyEntity>>any())
                .optional())
                .thenReturn(Optional.of(entity));
        DummyEntity saved = repository.save(entity);
        assertThat(saved.getId()).isEqualTo(1L);
    }

    @Test
    void deleteById_shouldCallJdbcUpdate() {
        when(jdbcClient.sql(anyString()).param(anyString(), any()).update()).thenReturn(1);
        int result = repository.deleteById(1L);
        assertThat(result).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void findAll_withNullFilters_shouldNotFail() {
        // Also cover the case when params(null) is called
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(Class.class)).single()).thenReturn(0L);
        // Cover both params(Map) and params(null) to avoid ambiguity
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(Class.class)).single()).thenReturn(0L);
        Pageable pageable = PageRequest.of(0, 1);
        // Make sure count query always returns 0L, not null
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(Class.class)).single()).thenReturn(0L);
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(RowMapper.class)).list()).thenReturn(List.of());
        Page<DummyEntity> page = repository.findAll(pageable, null);
        assertThat(page.getTotalElements()).isZero();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findAll_withEmptyFilters_shouldNotFail() {
        Pageable pageable = PageRequest.of(0, 1);
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(Class.class)).single()).thenReturn(0L);
        when(jdbcClient.sql(anyString()).params(anyMap()).query(any(RowMapper.class)).list()).thenReturn(List.of());
        Page<DummyEntity> page = repository.findAll(pageable, Map.of());
        assertThat(page.getTotalElements()).isZero();
    }

    // Dummy entity and repository for testing
    static class DummyEntity extends com.example.crud.common.model.BaseEntity<Long> {
        private String name;

        public DummyEntity(Long id, String name) {
            setId(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class DummyRepository extends AbstractJdbcRepository<DummyEntity, Long> {

        DummyRepository(DataSource ds, JdbcClient jdbcClient) {
            super(ds, jdbcClient);
        }

        @Override
        public DummyEntity save(DummyEntity entity) {
            // Simulate save without accessing DB metadata
            entity.setId(1L);
            return entity;
        }

        @Override
        protected String getTableName() {
            return "dummy";
        }

        @Override
        protected String getIdColumnName() {
            return "id";
        }

        @Override
        protected RowMapper<DummyEntity> getRowMapper() {
            return (rs, rn) -> new DummyEntity(rs.getLong("id"), rs.getString("name"));
        }

        @Override
        protected Map<String, Object> getUpdateParameters(DummyEntity entity) {
            return Map.of("name", entity.getName());
        }

        @Override
        protected Set<String> getAllowedSortColumns() {
            return Set.of("name");
        }
    }
}
