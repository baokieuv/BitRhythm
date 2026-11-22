package com.bit_rhythm.content_service.common.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

public final class ResultSetUtils {

    private ResultSetUtils() {
    }

    /** Returns lowercase column labels present in the ResultSet (build once per row for O(1) lookups). */
    public static Set<String> columnLabels(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        Set<String> labels = HashSet.newHashSet(count);
        for (int i = 1; i <= count; i++) {
            labels.add(meta.getColumnLabel(i).toLowerCase());
        }
        return labels;
    }

    public static boolean hasColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getString(ResultSet rs, String col, String defaultVal) throws SQLException {
        if (hasColumn(rs, col)) return defaultVal;
        String val = rs.getString(col);
        return val != null ? val : defaultVal;
    }

    public static Long getLong(ResultSet rs, String col, Long defaultVal) throws SQLException {
        if (hasColumn(rs, col)) return defaultVal;
        Long val = rs.getObject(col, Long.class);
        return val != null ? val : defaultVal;
    }

    public static Integer getInt(ResultSet rs, String col, Integer defaultVal) throws SQLException {
        if (hasColumn(rs, col)) return defaultVal;
        Integer val = rs.getObject(col, Integer.class);
        return val != null ? val : defaultVal;
    }

    public static boolean getBoolean(ResultSet rs, String col, boolean defaultVal) throws SQLException {
        if (hasColumn(rs, col)) return defaultVal;
        Boolean val = rs.getObject(col, Boolean.class);
        return val != null ? val : defaultVal;
    }

    public static Instant getInstant(ResultSet rs, String col) throws SQLException {
        if (hasColumn(rs, col)) return null;
        Timestamp ts = rs.getTimestamp(col);
        return ts != null ? ts.toInstant() : null;
    }

    public static LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        Instant instant = getInstant(rs, col);
        return instant != null ? instant.atZone(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    public static <E extends Enum<E>> E getEnum(ResultSet rs, String col, Class<E> enumType, E defaultVal) throws SQLException {
        if (hasColumn(rs, col)) return defaultVal;
        String val = rs.getString(col);
        return val != null ? Enum.valueOf(enumType, val) : defaultVal;
    }
}