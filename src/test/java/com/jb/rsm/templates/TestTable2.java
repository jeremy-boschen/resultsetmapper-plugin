package com.jb.rsm.templates;

import java.sql.SQLException;
import java.util.Objects;

import jdbc_template.ResultSetWrapper;

public class TestTable2 {
    protected String jsonbColumn;

    public void setJsonbColumn(String jsonbColumn) {
        this.jsonbColumn = jsonbColumn;
    }

    public static TestTable2 fromRow(ResultSetWrapper rsw) throws SQLException {
        TestTable2 e = new TestTable2();
        e.jsonbColumn = rsw.getString("jsonbcolumn");
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestTable2 that = (TestTable2) o;
        return Objects.equals(jsonbColumn, that.jsonbColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jsonbColumn);
    }

    @Override
    public String toString() {
        return "TestTable2{" +
               "\n jsonbColumn='" + jsonbColumn + '\'' +
               "\n}";
    }
}
