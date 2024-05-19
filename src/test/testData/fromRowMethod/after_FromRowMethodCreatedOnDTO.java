package com.jb.test;

import com.jb.jdbc.ResultSetWrapper;

public class MyDTO {
    private String columnOne;
    private Integer columnTwo;


    @javax.annotation.Generated("ResultSetMapperIntention")
    public static MyDTO fromRow(ResultSetWrapper rsw) throws java.sql.SQLException {
        MyDTO e = new MyDTO();
        e.columnOne = rsw.getString("columnone");
        e.columnTwo = rsw.getInt("columntwo");
        return e;
    }
}
