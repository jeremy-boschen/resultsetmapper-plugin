-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE public.test_table
(
    id_column            SERIAL PRIMARY KEY,
    bit_column           BIT,              -- JDBC Bit
    boolean_column       BOOLEAN,          -- JDBC Boolean
    tinyint_column       SMALLINT,         -- JDBC TinyInt
    smallint_column      SMALLINT,         -- JDBC SmallInt
    integer_column       INTEGER,          -- JDBC Integer
    bigint_column        BIGINT,           -- JDBC BigInt
    float_column         REAL,             -- JDBC Float
    double_column        DOUBLE PRECISION, -- JDBC Double
    numeric_column       NUMERIC,          -- JDBC Numeric
    decimal_column       DECIMAL,          -- JDBC Decimal
    char_column          CHAR(1),          -- JDBC Char
    varchar_column       VARCHAR(255),     -- JDBC VarChar
    longvarchar_column   TEXT,             -- JDBC LongVarChar
    date_column          DATE,             -- JDBC Date
    time_column          TIME,             -- JDBC Time
    timestamp_column     TIMESTAMP,        -- JDBC Timestamp
    binary_column        BYTEA,            -- JDBC Binary
    varbinary_column     BYTEA,            -- JDBC VarBinary
    longvarbinary_column BYTEA,            -- JDBC LongVarBinary
    blob_column          BYTEA,            -- JDBC Blob
    clob_column          TEXT,             -- JDBC Clob
    array_column         INTEGER[],        -- JDBC Array
    jsonb_column         JSONB,            -- JDBC JSON/JSONB
    xml_column           XML               -- JDBC SQL XML
);

INSERT INTO public.test_table (bit_column, boolean_column, tinyint_column, smallint_column, integer_column,
                               bigint_column, float_column, double_column, numeric_column, decimal_column, char_column,
                               varchar_column, longvarchar_column, date_column, time_column, timestamp_column,
                               binary_column, varbinary_column, longvarbinary_column, blob_column, clob_column,
                               array_column, xml_column, jsonb_column)
VALUES (B'1', TRUE, 127, 32767, 2147483647, 9223372036854775807, 123.456, 123456.7890123, 123456.789, 123456.789,
        'c', 'varchar data', 'long varchar data', '2024-05-01', '12:34:56', '2024-05-01 12:34:56.789012',
        '\x48656c6c6f', '\x48656c6c6f', '\x48656c6c6f', 'long blob data', 'long clob data', ARRAY[1, 2, 3],
        '<root><child>example</child></root>', '{"key": "value"}');

INSERT INTO public.test_table (bit_column, boolean_column, tinyint_column, smallint_column, integer_column,
                               bigint_column, float_column, double_column, numeric_column, decimal_column, char_column,
                               varchar_column, longvarchar_column, date_column, time_column, timestamp_column,
                               binary_column, varbinary_column, longvarbinary_column, blob_column, clob_column,
                               array_column, xml_column, jsonb_column)
VALUES (B'0', FALSE, -127, 123, 987654321, 123456789012345, 789.012, 789012.123456, 987654.321, 987654.321, 'd',
        'test varchar', 'test long varchar', '2024-05-10', '23:59:59', '2024-05-10 23:59:59.999999', '\x48656c6c6f',
        '\x48656c6c6f', '\x48656c6c6f', 'long blob data', 'some clob data', ARRAY[4, 5, 6],
        '<data><value>more</value></data>', '{"array": [1,2,3], "boolean": true}');
