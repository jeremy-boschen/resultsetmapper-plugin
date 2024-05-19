package com.jb.rsm.templates;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

import jdbc_template.ResultSetWrapper;

public class TestTable1 {
    public Boolean bitColumn;
    public Boolean booleanColumn;
    public Short tinyintColumn;
    public Short smallintColumn;
    public Integer integerColumn;
    public Long bigintColumn;
    public Float floatColumn;
    public Double doubleColumn;
    public BigDecimal numericColumn;
    public BigDecimal decimalColumn;
    public char charColumn;
    public String varcharColumn;
    public String longvarcharColumn;
    public Date dateColumn;
    public Time timeColumn;
    public Timestamp timestampColumn;
    public byte[] binaryColumn;
    public byte[] varbinaryColumn;
    public byte[] longvarbinaryColumn;
    public byte[] blobColumn;
    public String clobColumn;
    public int[] arrayColumn;
    public TestTable2 t2 = new TestTable2();



    public void setBitColumn(Boolean bitColumn) {
        this.bitColumn = bitColumn;
    }

    public void setBooleanColumn(Boolean booleanColumn) {
        this.booleanColumn = booleanColumn;
    }

    public void setTinyintColumn(Short tinyintColumn) {
        this.tinyintColumn = tinyintColumn;
    }

    public void setSmallintColumn(Short smallintColumn) {
        this.smallintColumn = smallintColumn;
    }

    public void setIntegerColumn(Integer integerColumn) {
        this.integerColumn = integerColumn;
    }

    public void setBigintColumn(Long bigintColumn) {
        this.bigintColumn = bigintColumn;
    }

    public void setFloatColumn(Float floatColumn) {
        this.floatColumn = floatColumn;
    }

    public void setDoubleColumn(Double doubleColumn) {
        this.doubleColumn = doubleColumn;
    }

    public void setNumericColumn(BigDecimal numericColumn) {
        this.numericColumn = numericColumn;
    }

    public void setDecimalColumn(BigDecimal decimalColumn) {
        this.decimalColumn = decimalColumn;
    }

    public void setCharColumn(char charColumn) {
        this.charColumn = charColumn;
    }

    public void setVarcharColumn(String varcharColumn) {
        this.varcharColumn = varcharColumn;
    }

    public void setLongvarcharColumn(String longvarcharColumn) {
        this.longvarcharColumn = longvarcharColumn;
    }

    public void setDateColumn(Date dateColumn) {
        this.dateColumn = dateColumn;
    }

    public void setTimeColumn(Time timeColumn) {
        this.timeColumn = timeColumn;
    }

    public void setTimestampColumn(Timestamp timestampColumn) {
        this.timestampColumn = timestampColumn;
    }

    public void setBinaryColumn(byte[] binaryColumn) {
        this.binaryColumn = binaryColumn;
    }

    public void setVarbinaryColumn(byte[] varbinaryColumn) {
        this.varbinaryColumn = varbinaryColumn;
    }

    public void setLongvarbinaryColumn(byte[] longvarbinaryColumn) {
        this.longvarbinaryColumn = longvarbinaryColumn;
    }

    public void setBlobColumn(byte[] blobColumn) {
        this.blobColumn = blobColumn;
    }

    public void setClobColumn(String clobColumn) {
        this.clobColumn = clobColumn;
    }


    public static TestTable1 fromRow(ResultSetWrapper rsw) throws java.sql.SQLException {
        TestTable1 e = new TestTable1();
        e.bitColumn = rsw.getBoolean("bitcolumn");
        e.booleanColumn = rsw.getBoolean("booleancolumn");
        e.tinyintColumn = rsw.getShort("tinyintcolumn");
        e.smallintColumn = rsw.getShort("smallintcolumn");
        e.integerColumn = rsw.getInt("integercolumn");
        e.bigintColumn = rsw.getLong("bigintcolumn");
        e.floatColumn = rsw.getFloat("floatcolumn");
        e.doubleColumn = rsw.getDouble("doublecolumn");
        e.numericColumn = rsw.getBigDecimal("numericcolumn");
        e.decimalColumn = rsw.getBigDecimal("decimalcolumn");
        e.charColumn = rsw.getChar("charcolumn");
        e.varcharColumn = rsw.getString("varcharcolumn");
        e.longvarcharColumn = rsw.getString("longvarcharcolumn");
        e.dateColumn = rsw.getDate("datecolumn");
        e.timeColumn = rsw.getTime("timecolumn");
        e.timestampColumn = rsw.getTimestamp("timestampcolumn");
        e.binaryColumn = rsw.getBinary("binarycolumn");
        e.varbinaryColumn = rsw.getBinary("varbinarycolumn");
        e.longvarbinaryColumn = rsw.getBinary("longvarbinarycolumn");
        e.blobColumn = rsw.getBinary("blobcolumn");
        e.clobColumn = rsw.getString("clobcolumn");
        e.arrayColumn = rsw.getArray("arraycolumn", int[].class);
        e.t2 = TestTable2.fromRow(rsw.withPrefix("t2"));
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestTable1 other = (TestTable1) o;
        boolean equals = Objects.equals(bitColumn, other.bitColumn);
        equals = equals && Objects.equals(charColumn, other.charColumn);
        equals = equals && Objects.equals(booleanColumn, other.booleanColumn);
        equals = equals && Objects.equals(tinyintColumn, other.tinyintColumn);
        equals = equals && Objects.equals(smallintColumn, other.smallintColumn);
        equals = equals && Objects.equals(integerColumn, other.integerColumn);
        equals = equals && Objects.equals(bigintColumn, other.bigintColumn);
        equals = equals && Objects.equals(floatColumn, other.floatColumn);
        equals = equals && Objects.equals(doubleColumn, other.doubleColumn);
        equals = equals && Objects.equals(numericColumn, other.numericColumn);
        equals = equals && Objects.equals(decimalColumn, other.decimalColumn);
        equals = equals && Objects.equals(charColumn, other.charColumn);
        equals = equals && Objects.equals(varcharColumn, other.varcharColumn);
        equals = equals && Objects.equals(longvarcharColumn, other.longvarcharColumn);
        equals = equals && Objects.equals(dateColumn, other.dateColumn);
        equals = equals && Objects.equals(timeColumn, other.timeColumn);
        equals = equals && Objects.equals(timestampColumn, other.timestampColumn);
        equals = equals && Objects.deepEquals(binaryColumn, other.binaryColumn);
        equals = equals && Objects.deepEquals(varbinaryColumn, other.varbinaryColumn);
        equals = equals && Objects.deepEquals(longvarbinaryColumn, other.longvarbinaryColumn);
        equals = equals && Objects.deepEquals(blobColumn, other.blobColumn);
        equals = equals && Objects.equals(clobColumn, other.clobColumn);
        equals = equals && Objects.deepEquals(arrayColumn, other.arrayColumn);
        equals = equals && Objects.equals(t2, other.t2);

        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitColumn, booleanColumn, tinyintColumn, smallintColumn, integerColumn, bigintColumn,
                floatColumn, doubleColumn, numericColumn, decimalColumn, charColumn, varcharColumn, longvarcharColumn,
                dateColumn, timeColumn, timestampColumn, Arrays.hashCode(binaryColumn), Arrays.hashCode(varbinaryColumn),
                Arrays.hashCode(longvarbinaryColumn), Arrays.hashCode(blobColumn), clobColumn, Arrays.hashCode(arrayColumn), t2);
    }

    @Override
    public String toString() {
        return "TestTable{" +
               "\n  bitColumn=" + bitColumn +
               "\n, booleanColumn=" + booleanColumn +
               "\n, tinyintColumn=" + tinyintColumn +
               "\n, smallintColumn=" + smallintColumn +
               "\n, integerColumn=" + integerColumn +
               "\n, bigintColumn=" + bigintColumn +
               "\n, floatColumn=" + floatColumn +
               "\n, doubleColumn=" + doubleColumn +
               "\n, numericColumn=" + numericColumn +
               "\n, decimalColumn=" + decimalColumn +
               "\n, charColumn='" + charColumn + '\'' +
               "\n, varcharColumn='" + varcharColumn + '\'' +
               "\n, longvarcharColumn='" + longvarcharColumn + '\'' +
               "\n, dateColumn=" + dateColumn +
               "\n, timeColumn=" + timeColumn +
               "\n, timestampColumn=" + timestampColumn +
               "\n, binaryColumn=" + Arrays.toString(binaryColumn) +
               "\n, varbinaryColumn=" + Arrays.toString(varbinaryColumn) +
               "\n, longvarbinaryColumn=" + Arrays.toString(longvarbinaryColumn) +
               "\n, blobColumn=" + Arrays.toString(blobColumn) +
               "\n, clobColumn='" + clobColumn + '\'' +
               "\n, arrayColumn=" + Arrays.toString(arrayColumn) +
               "\n, t2=" + t2 +
               "\n}";
    }
}
