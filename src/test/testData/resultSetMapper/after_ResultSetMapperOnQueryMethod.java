package com.jb.test;


import java.util.List;

import com.jb.jdbc.ResultSetMapper;
import com.jb.jdbc.ResultSetWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;


public class MyTest {
    public static class MyDTO {
        private String name;
        private Integer value;

        @javax.annotation.Generated("ResultSetMapperIntention")
        public static MyDTO fromRow(ResultSetWrapper rsw) throws java.sql.SQLException {
            MyDTO e = new MyDTO();
            e.name = rsw.getString("name");
            e.value = rsw.getInt("value");
            return e;
        }
    }

    public List<MyDTO> getMyDTOs(JdbcTemplate jdbc) {
        List<MyDTO> values = jdbc.<List<MyDTO>>query("select * from mytable", ResultSetMapper.using(MyDTO::fromRow, MyDTO.class));
        return values;
    }
}
