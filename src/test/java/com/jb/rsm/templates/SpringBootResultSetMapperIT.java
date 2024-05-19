package com.jb.rsm.templates;


import java.sql.SQLException;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jdbc_template.ResultSetMapper;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {SpringBootResultSetMapperIT.class},
        properties = {"logging.level.org.springframework.jdbc.core=TRACE"}
)
class SpringBootResultSetMapperIT {

    @Container
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:13.3")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/init.sql");


    private NamedParameterJdbcTemplate jdbc = null;

    @BeforeEach
    void setUp() {
        database.start();

        jdbc = new NamedParameterJdbcTemplate(
                DataSourceBuilder.create()
                        .url(database.getJdbcUrl())
                        .username(database.getUsername())
                        .password(database.getPassword())
                        .driverClassName(database.getDriverClassName())
                        .build());
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (null != jdbc) {
            if (null != jdbc.getJdbcTemplate().getDataSource()) {
                JdbcUtils.closeConnection(jdbc.getJdbcTemplate().getDataSource().getConnection());
            }
        }

        database.stop();
    }

    @Test
    @SuppressWarnings("SqlNoDataSourceInspection")
    void testCompareToBeanPropertyRowMapper() throws SQLException {
        String sql = """
            SELECT
              bit_column
            , boolean_column
            , tinyint_column
            , smallint_column
            , integer_column
            , bigint_column
            , float_column
            , double_column
            , numeric_column
            , decimal_column
            , char_column
            , varchar_column
            , longvarchar_column
            , date_column
            , time_column
            , timestamp_column
            , binary_column
            , varbinary_column
            , longvarbinary_column
            , blob_column
            , clob_column
            -- , array_column
            -- , jsonb_column AS "t2.jsonb_column"
            -- , xml_column
            FROM
              public.test_table
            ORDER BY
              id_column
        """;

        var beanPropertyRowMapperResult = jdbc.query(sql, new BeanPropertyRowMapper<>(TestTable1.class));
        var fromRowMapperResult = jdbc.query(sql, ResultSetMapper.using(TestTable1::fromRow, TestTable1.class));

        // Assert that results are equal
        for (int i = 0; i < beanPropertyRowMapperResult.size(); i++) {
            var beanResult = beanPropertyRowMapperResult.get(i);
            //noinspection DataFlowIssue - query() cannot return null
            var rsResult = fromRowMapperResult.get(i);
            Assertions.assertEquals(beanResult, rsResult);
        }
    }
}
