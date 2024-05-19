package jdbc_template;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a {@link ResultSet} providing safe access for columns that may not exist as well
 * as helpers to simplify common operations. <br/>
 * <p/>
 * This class supports several advantages over the standard ResultSet class.
 * <ol>
 *     <li>Columns not found in the ResultSet will not generate an exception</li>
 *     <li>Support for nested extraction via {@link #withPrefix}</li>
 *     <li>Simplified Array/List extraction</li>
 *     <li>Support for diagnostic information: missing/found/not-found columns</li>
 *     <li>Better performance over BeanPropertyRowMapper by doing column mapping once, and manual assignment via a {@link FromRowMapper#fromRow}</li>
 * </ol>
 */
@Generated("generator_name")
@SuppressWarnings("unused")
public class ResultSetWrapper {

    private static final Logger logger = LoggerFactory.getLogger(ResultSetWrapper.class);

    private final ResultSet rs;
    private final Map<String, Column> columns;
    private final String prefix;
    private final Set<String> missing;

    public ResultSetWrapper(ResultSet rs) throws SQLException {
        this.rs = rs;
        this.columns = getColumnMap(rs.getMetaData());
        this.prefix = "";
        this.missing = new HashSet<>();

        if (logger.isDebugEnabled()) {
            logger.debug("Loaded columns: {}", columns.keySet());
        }
    }

    private ResultSetWrapper(ResultSetWrapper rsw, String prefix) {
        this.rs = rsw.rs;
        this.columns = rsw.columns;
        this.prefix = prefix;
        this.missing = rsw.missing;
    }

    private static Map<String, Column> getColumnMap(java.sql.ResultSetMetaData metaData)
        throws java.sql.SQLException {
        var columns = new HashMap<String, Column>();

        // Per the JDBC spec, if a column is duplicated in the result set, the first occurrence should be used. We walk
        // from the end of the list to the beginning, causing duplicates to be overwritten by earlier values.
        for (int colIdx = metaData.getColumnCount(); colIdx >= 1; colIdx--) {
            var column = new Column(metaData.getColumnType(colIdx), colIdx,
                metaData.getColumnName(colIdx));
            // The lookup name is required to be the field name in lowercase with spaces and underscores removed
            var lookupName = column.name.toLowerCase().replace(" ", "").replace("_", "");
            columns.put(lookupName, column);
        }
        return columns;
    }

    /**
     * Creates a new ResultSetWrapper that filters the columns to only those starting with the given
     * prefix. Can be used for populating nested objects from a single result set.
     *
     * @param columnPrefix the column prefix
     */
    public ResultSetWrapper withPrefix(String columnPrefix) {
        if (columnPrefix == null || columnPrefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }

        return new ResultSetWrapper(
            this,
            columnPrefix + ('.' == columnPrefix.charAt(columnPrefix.length() - 1) ? "" : "."));
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return rs.getMetaData();
    }

    public List<String> getColumns() {
        return columns.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public boolean next() throws SQLException {
        return rs.next();
    }

    public boolean wasNull() throws SQLException {
        return rs.wasNull();
    }

    public String getString(String column, String... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            String value = rs.getString(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    /**
     * @implNote If no defaultVal is provided, and the column's value was NULL, or an empty string,
     * then the character value of \0 is returned.
     */
    public char getChar(String column, char... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            String value = rs.getString(col.index);
            return !rs.wasNull() && !value.isEmpty() ? value.charAt(0)
                : defaultVal.length == 0 ? '\0' : defaultVal[0];
        }
        return defaultVal.length == 0 ? '\0' : defaultVal[0];
    }

    public byte getByte(String column, byte... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            byte value = rs.getByte(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0 : defaultVal[0];
    }

    public short getShort(String column, short... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            short value = rs.getShort(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0 : defaultVal[0];
    }

    public int getInt(String column, int... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            int value = rs.getInt(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0 : defaultVal[0];
    }

    public long getLong(String column, long... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            long value = rs.getLong(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0 : defaultVal[0];
    }

    public float getFloat(String column, float... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            float value = rs.getFloat(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0.0f : defaultVal[0];
    }

    public double getDouble(String column, double... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            double value = rs.getDouble(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? 0 : defaultVal[0];
        }
        return defaultVal.length == 0 ? 0.0f : defaultVal[0];
    }

    public BigDecimal getBigDecimal(String column, BigDecimal... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            BigDecimal value = rs.getBigDecimal(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public boolean getBoolean(String column, boolean... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            boolean value = rs.getBoolean(col.index);
            return !rs.wasNull() ? value : defaultVal.length != 0 && defaultVal[0];
        }
        return defaultVal.length != 0 && defaultVal[0];
    }

    public Date getDate(String column, Date... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            Date value = rs.getDate(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public Time getTime(String column, Time... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            Time value = rs.getTime(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public Timestamp getTimestamp(String column, Timestamp... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            Timestamp value = rs.getTimestamp(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public LocalDate getLocalDate(String column, LocalDate... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            // Most drivers support converting values to LocalDate
            var value = rs.getObject(col.index, LocalDate.class);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }

        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public LocalDateTime getLocalDateTime(String column, LocalDateTime... defaultVal)
        throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            // Most drivers support converting values to LocalDateTime
            var value = rs.getObject(col.index, LocalDateTime.class);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public OffsetDateTime getOffsetDateTime(String column, OffsetDateTime... defaultVal)
        throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            // Most drivers support converting values to OffsetDateTime
            OffsetDateTime value = rs.getObject(col.index, OffsetDateTime.class);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public UUID getUUID(String column, UUID... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            // Most drivers support converting values to OffsetDateTime
            UUID value = rs.getObject(col.index, UUID.class);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public byte[] getBinary(String column) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            byte[] value = rs.getBytes(col.index);
            return !rs.wasNull() ? value : null;
        }
        return null;
    }

    public Object getObject(String column, Object... defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            Object value = rs.getObject(col.index);
            return !rs.wasNull() ? value : defaultVal.length == 0 ? null : defaultVal[0];
        }
        return defaultVal.length == 0 ? null : defaultVal[0];
    }

    public <T> T getObject(String column, Class<T> type) throws SQLException {
        return getObject(column, type, null);
    }

    public <T> T getObject(String column, Class<T> type, T defaultVal) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            T value = rs.getObject(col.index, type);
            return !rs.wasNull() ? value : defaultVal;
        }
        return defaultVal;
    }

    public Array getArray(String column) throws SQLException {
        Column col = findColumn(column);
        if (col != null) {
            Array value = rs.getArray(col.index);
            return !rs.wasNull() ? value : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArray(String column, Class<T> type) throws SQLException {
        Array array = getArray(column);
        if (null != array) {
            try {
                Object value = array.getArray();
                if (type.isAssignableFrom(value.getClass())) {
                    return (T) value;
                }

                var source = (Object[]) value;
                var target = java.lang.reflect.Array.newInstance(type.getComponentType(),
                    source.length);
                for (int i = 0; i < source.length; i++) {
                    // Support automatic unwrapping
                    java.lang.reflect.Array.set(target, i, source[i]);
                }
                return (T) target;
            } finally {
                array.free();
            }
        }
        return null;
    }

    public <T> List<T> getList(String column, Class<T> type) throws SQLException {
        return getList(column, type, List.of());
    }

    public <T> List<T> getList(String column, Class<T> type, List<T> defaultVal)
        throws SQLException {
        var array = getArray(column, type);
        if (array != null) {
            return List.of(array);
        }
        return defaultVal;
    }

    public <T> Set<T> getSet(String column, Class<T> type) throws SQLException {
        return getSet(column, type, Set.of());
    }

    public <T> Set<T> getSet(String column, Class<T> type, Set<T> defaultVal) throws SQLException {
        var array = getArray(column, type);
        if (array != null) {
            return Set.of(array);
        }
        return defaultVal;
    }

    private Column findColumn(String columnName) {
        var lookupName = prefix + columnName;
        var column = columns.get(lookupName);
        if (null != column) {
            column.accessed += 1;
        }

        return column;
    }

    /**
     * Logs diagnostic usage information about column utilization within this instance.
     * <p/>
     * <br/> - If columns required by the entity class are missing from the ResultSet, a warning
     * level log entry will be emitted containing a list of missing columns, in addition to lists of
     * used and unused columns. <br/> - If no columns were missing from the ResultSet, a debug level
     * log entry will be emitted listing the used and unused columns.
     * <pre>Example:
     * WARN c.p.l.c.j.ResultSetWrapper - Usage for MyDTO: missing=[column3], used=[column1,column2], unused[]
     * </pre>
     * Missing columns should be considered a bug. Unused columns can be removed if possible from
     * queries when possible.
     */
    void logUsageDiagnostics(String from) {
        if (!missing.isEmpty() || logger.isDebugEnabled()) {
            var used = new ArrayList<String>();
            var unused = new ArrayList<String>();

            columns.forEach((name, column) -> {
                if (column.accessed > 0) {
                    used.add(column.name);
                } else {
                    unused.add(column.name);
                }
            });

            if (!missing.isEmpty()) {
                logger.warn("Usage for {}: missing={}, unused={}, used={}",
                    from,
                    missing,
                    unused,
                    used);
            } else {
                logger.debug("Usage for {}: unused={}, used={}",
                    from,
                    unused,
                    used);
            }
        }
    }

    private static class Column implements Comparable<Column> {

        public int type;
        public int index;
        public String name;
        public int accessed;

        public Column(int type, int index, String name) {
            this.type = type;
            this.index = index;
            this.name = name;
        }

        @Override
        public int compareTo(Column o) {
            return index - o.index;
        }
    }

    /**
     * Used by {@link ResultSetMapper#using} and {@link FromRowMapper#using} to log column usage
     * upon completion of mapping.
     */
    public static class DiagnosticResultSetWrapper<T> extends ResultSetWrapper implements
        AutoCloseable {

        private final Class<T> type;

        public DiagnosticResultSetWrapper(ResultSet rs, Class<T> type) throws SQLException {
            super(rs);
            this.type = type;
        }

        @Override
        public void close() {
            logUsageDiagnostics(type.getSimpleName());
        }
    }
}