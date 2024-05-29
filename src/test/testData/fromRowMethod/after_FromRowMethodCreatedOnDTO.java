package com.jb.test;

import com.jb.jdbc.ResultSetWrapper;

public class MyDTO {
    public static MyDTO2 {
        private String valueOne;
    }

    private String columnOne;
    private Integer columnTwo;
    private long columnThree;
    private List<Long> columnFor;
    private Set<MyDTO2> columnFive;

    @javax.annotation.Generated("ResultSetMapperIntention")
    public static MyDTO fromRow(ResultSetWrapper rsw) throws java.sql.SQLException {
        MyDTO e = new MyDTO();
        e.columnOne = rsw.getString("columnone");
        e.columnTwo = rsw.getInt("columntwo");
        e.columnThree = rsw.getLong("columnthree", e.columnThree);
        //TODO: No ResultSetWrapper mapping exists for columnFor:List<Long>
        //TODO: No ResultSetWrapper mapping exists for columnFive:Set<MyDTO2>
        return e;
    }
}
