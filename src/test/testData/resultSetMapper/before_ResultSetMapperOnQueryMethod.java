package com.jb.test;


import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;


public class MyTest {
    public static class MyDTO {
        private String name;
        private Integer value;
        private byte code;
    }

    public List<MyDTO> getMyDTOs(JdbcTemplate jdbc) {
        List<MyDTO> values = jdbc.<List<MyDTO>>query("select * from mytable", <caret>);
        return values;
    }
}
