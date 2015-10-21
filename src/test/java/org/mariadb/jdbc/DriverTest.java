package org.mariadb.jdbc;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;


public class DriverTest extends BaseTest {
    /**
     * Tables initialisation.
     * @throws SQLException exception
     */
    @BeforeClass()
    public static void initClass() throws SQLException {
        createTable("tt1", "id int , name varchar(20)");
        createTable("tt2", "id int , name varchar(20)");
        createTable("Drivert2", "id int not null primary key auto_increment, test varchar(10)");
        createTable("Drivert3", "id int not null primary key auto_increment, test varchar(10)");
        createTable("Drivert30", "id int not null primary key auto_increment, test varchar(20)", "engine=innodb");
        createTable("Drivert4", "id int not null primary key auto_increment, test varchar(20)", "engine=innodb");
        createTable("test_batch", "id int not null primary key auto_increment, test varchar(10)");
        createTable("test_batch2", "id int not null primary key auto_increment, test varchar(10)");
        createTable("test_float", "id int not null primary key auto_increment, a float");
        createTable("test_big_autoinc2", "id int not null primary key auto_increment, test varchar(10)");
        createTable("test_big_update", "id int primary key not null, updateme int");
        createTable("Driverstreamtest", "id int not null primary key, strm text");
        createTable("Driverstreamtest2", "id int primary key not null, strm text");
        createTable("sharedConnection", "id int");
        createTable("objecttest", "int_test int primary key not null, string_test varchar(30), "
                + "timestamp_test timestamp, serial_test blob");
        createTable("bintest", "id int not null primary key auto_increment, bin1 varbinary(300), bin2 varbinary(300)");
        createTable("genkeys", "priKey INT NOT NULL AUTO_INCREMENT, dataField VARCHAR(64), PRIMARY KEY (priKey)");
        createTable("extest", "id int not null primary key");
        createTable("rewritetest", "id int not null primary key, a varchar(10), b int", "engine=innodb");
        createTable("rewritetest2", "id int not null primary key, a varchar(10), b int", "engine=innodb");
        createTable("commentPreparedStatements", "id int not null primary key auto_increment, a varchar(10)");
        createTable("quotesPreparedStatements", "id int not null primary key auto_increment, a varchar(10) , "
                + "b varchar(10)");
        createTable("bigdectest", "id int not null primary key auto_increment, bd decimal", "engine=innodb");
        createTable("bytetest", "id int not null primary key auto_increment, a int", "engine=innodb");
        createTable("shorttest", "id int not null primary key auto_increment,a int", "engine=innodb");
        createTable("doubletest", "id int not null primary key auto_increment,a double", "engine=innodb");
        createTable("ressetpos", "i int not null primary key", "engine=innodb");
        createTable("blabla", "valsue varchar(20)");
        createTable("bug501452", "id int not null primary key, value varchar(20)");
        createTable("test_setobjectconv", "id int not null primary key auto_increment, v1 varchar(40), v2 varchar(40)");
        createTable("bittest", "id int not null primary key auto_increment, b int");
        createTable("streamingtest", "val varchar(20)");
        createTable("testBlob2", "a blob");
        createTable("testString2", "a varchar(10)");
        createTable("testBlob2", "a blob");
        createTable("unsignedtest", "a int unsigned");
        createTable("conj25", "a VARCHAR(1024)");
        createTable("batchUpdateException", "i int,PRIMARY KEY (i)");
        createTable("emptytest", "id int");
        createTable("DriverTestt1", "id int not null primary key auto_increment, test varchar(20)");
        createTable("DriverTestt2", "id int not null primary key auto_increment, test varchar(20)");
        createTable("DriverTestt3", "id int not null primary key auto_increment, test varchar(20)");
        createTable("DriverTestt4", "id int not null primary key auto_increment, test varchar(20)");
        createTable("DriverTestt5", "id int not null primary key auto_increment, test varchar(20)");
    }

    @Test
    public void doQuery() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("insert into DriverTestt1 (test) values ('hej1')");
        stmt.execute("insert into DriverTestt1 (test) values ('hej2')");
        stmt.execute("insert into DriverTestt1 (test) values ('hej3')");
        stmt.execute("insert into DriverTestt1 (test) values (null)");
        ResultSet rs = stmt.executeQuery("select * from DriverTestt1");
        for (int i = 1; i < 4; i++) {
            rs.next();
            assertEquals(String.valueOf(i), rs.getString(1));
            assertEquals("hej" + i, rs.getString("test"));
        }
        rs.next();
        assertEquals(null, rs.getString("test"));
    }

    @Test(expected = SQLException.class)
    public void askForBadColumnTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("insert into DriverTestt2 (test) values ('hej1')");
        stmt.execute("insert into DriverTestt2 (test) values ('hej2')");
        stmt.execute("insert into DriverTestt2 (test) values ('hej3')");
        stmt.execute("insert into DriverTestt2 (test) values (null)");
        ResultSet rs = stmt.executeQuery("select * from DriverTestt2");
        rs.next();
        rs.getInt("non_existing_column");
    }

    @Test(expected = SQLException.class)
    public void askForBadColumnIndexTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("insert into DriverTestt3 (test) values ('hej1')");
        stmt.execute("insert into DriverTestt3 (test) values ('hej2')");
        stmt.execute("insert into DriverTestt3 (test) values ('hej3')");
        stmt.execute("insert into DriverTestt3 (test) values (null)");
        ResultSet rs = stmt.executeQuery("select * from DriverTestt3");
        rs.next();
        rs.getInt(102);
    }

    @Test
    /* Accessing result set using  table.column */
    public void tableDotColumnInResultSet() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("insert into tt1 values(1, 'one')");
        stmt.execute("insert into tt2 values(1, 'two')");
        ResultSet rs = stmt.executeQuery("select tt1.*, tt2.* from tt1, tt2 where tt1.id = tt2.id");
        rs.next();
        Assert.assertEquals(1, rs.getInt("tt1.id"));
        Assert.assertEquals(1, rs.getInt("tt2.id"));
        Assert.assertEquals("one", rs.getString("tt1.name"));
        Assert.assertEquals("two", rs.getString("tt2.name"));
    }

    @Test(expected = SQLException.class)
    public void badQuery() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.executeQuery("whraoaooa");
    }

    @Test
    public void preparedTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("insert into DriverTestt4 (test) values ('hej1')");
        stmt.close();

        String query = "SELECT * FROM DriverTestt4 WHERE test = ? and id = ?";
        PreparedStatement prepStmt = sharedConnection.prepareStatement(query);
        prepStmt.setString(1, "hej1");
        prepStmt.setInt(2, 1);
        ResultSet results = prepStmt.executeQuery();
        String res = "";
        while (results.next()) {
            res = results.getString("test");
        }
        assertEquals("hej1", res);
        assertEquals(2, prepStmt.getParameterMetaData().getParameterCount());
    }


    @Test
    public void streamingResultSet() throws Exception {
        Statement stmt = sharedConnection.createStatement();
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery("SELECT 1");
        assertTrue(rs.isBeforeFirst());
        try {
            rs.first();
            assertFalse("should not get there", true);
        } catch (SQLException sqle) {
            assertTrue(sqle.getMessage().toLowerCase().contains("invalid operation"));
        }
    }

    @Test
    public void updateTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();

        stmt.execute("insert into DriverTestt5 (test) values ('hej1')");
        stmt.execute("insert into DriverTestt5 (test) values ('hej2')");
        stmt.execute("insert into DriverTestt5 (test) values ('hej3')");
        stmt.execute("insert into DriverTestt5 (test) values (null)");

        String query = "UPDATE DriverTestt5 SET test = ? where id = ?";
        PreparedStatement prepStmt = sharedConnection.prepareStatement(query);
        prepStmt.setString(1, "updated");
        prepStmt.setInt(2, 3);
        int updateCount = prepStmt.executeUpdate();
        assertEquals(1, updateCount);
        String query2 = "SELECT * FROM DriverTestt5 WHERE id=?";
        prepStmt = sharedConnection.prepareStatement(query2);
        prepStmt.setInt(1, 3);
        ResultSet results = prepStmt.executeQuery();
        String result = "";
        while (results.next()) {
            result = results.getString("test");
        }
        assertEquals("updated", result);
    }

    @Test
    public void ralfTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        for (int i = 0; i < 10; i++) {
            stmt.execute("INSERT INTO Drivert2 (test) VALUES ('aßa" + i + "')");
        }
        PreparedStatement ps = sharedConnection.prepareStatement("SELECT * FROM Drivert2 where test like'%ß%' limit ?");
        ps.setInt(1, 5);
        ps.addBatch();
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
        }
        assertEquals(result, 5);
    }

    @Test
    public void autoIncTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("INSERT INTO Drivert3 (test) VALUES ('aa')", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        assertTrue(rs.next());

        assertEquals(1, rs.getInt(1));
        assertEquals(1, rs.getInt("insert_id"));

        stmt.execute("INSERT INTO Drivert3 (test) VALUES ('aa')");
        rs = stmt.getGeneratedKeys();
        assertTrue(rs.next());

        assertEquals(2, rs.getInt(1));
        assertEquals(2, rs.getInt("insert_id"));
        
        
        /* multi-row inserts */
        stmt.execute("INSERT INTO Drivert3 (test) VALUES ('bb'),('cc'),('dd')");
        rs = stmt.getGeneratedKeys();
        for (int i = 0; i < 3; i++) {
            assertTrue(rs.next());
            assertEquals(3 + i, rs.getInt(1));
        }

        requireMinimumVersion(5, 0);
        /* non-standard autoIncrementIncrement */
        int autoIncrementIncrement = 2;
        Connection connection = null;
        try {
            connection = setConnection("&sessionVariables=auto_increment_increment=" + autoIncrementIncrement);
            stmt = connection.createStatement();
            stmt.execute("INSERT INTO Drivert3 (test) values ('bb'),('cc')", Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();
            assertTrue(rs.next());
            assertEquals(7, rs.getInt(1));
            assertTrue(rs.next());
            assertEquals(7 + autoIncrementIncrement, rs.getInt(1));
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void transactionTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        sharedConnection.setAutoCommit(false);
        stmt.executeUpdate("INSERT INTO Drivert30 (test) VALUES ('heja')");
        stmt.executeUpdate("INSERT INTO Drivert30 (test) VALUES ('japp')");
        sharedConnection.commit();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Drivert30");
        assertEquals(true, rs.next());
        assertEquals("heja", rs.getString("test"));
        assertEquals(true, rs.next());
        assertEquals("japp", rs.getString("test"));
        assertEquals(false, rs.next());
        stmt.executeUpdate("INSERT INTO Drivert30 (test) VALUES ('rollmeback')", Statement.RETURN_GENERATED_KEYS);
        ResultSet rsGen = stmt.getGeneratedKeys();
        rsGen.next();
        assertEquals(3, rsGen.getInt(1));
        sharedConnection.rollback();
        rs = stmt.executeQuery("SELECT * FROM Drivert30 WHERE id=3");
        assertEquals(false, rs.next());
        sharedConnection.setAutoCommit(true);
    }

    @Test
    public void savepointTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        sharedConnection.setAutoCommit(false);
        stmt.executeUpdate("INSERT INTO Drivert4 (test) values('hej1')");
        stmt.executeUpdate("INSERT INTO Drivert4 (test) values('hej2')");
        Savepoint savepoint = sharedConnection.setSavepoint("yep");
        stmt.executeUpdate("INSERT INTO Drivert4 (test)  values('hej3')");
        stmt.executeUpdate("INSERT INTO Drivert4 (test) values('hej4')");
        sharedConnection.rollback(savepoint);
        stmt.executeUpdate("INSERT INTO Drivert4 (test) values('hej5')");
        stmt.executeUpdate("INSERT INTO Drivert4 (test) values('hej6')");
        sharedConnection.commit();
        ResultSet rs = stmt.executeQuery("SELECT * FROM Drivert4");
        assertEquals(true, rs.next());
        assertEquals("hej1", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("hej2", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("hej5", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("hej6", rs.getString(2));
        assertEquals(false, rs.next());
        sharedConnection.setAutoCommit(true);
    }

    @Test
    public void isolationLevel() throws SQLException {
        Connection connection = null;
        try {
            connection = setConnection();
            int[] levels = new int[]{
                    Connection.TRANSACTION_READ_UNCOMMITTED,
                    Connection.TRANSACTION_READ_COMMITTED,
                    Connection.TRANSACTION_SERIALIZABLE,
                    Connection.TRANSACTION_REPEATABLE_READ
            };
            for (int level : levels) {
                connection.setTransactionIsolation(level);
                assertEquals(level, connection.getTransactionIsolation());
            }
        } finally {
            connection.close();
        }
    }

    @Test
    public void isValidTest() throws SQLException {
        assertEquals(true, sharedConnection.isValid(0));
    }


    @Test
    public void testConnectNoDb() throws Exception {
        Connection connection = null;
        try {
            connection = setConnection();
            connection.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testConnectorJurl() throws SQLException {
        UrlParser url = UrlParser.parse("jdbc:mysql://localhost/test");
        assertEquals("localhost", url.getHostAddresses().get(0).host);
        assertEquals("test", url.getDatabase());
        assertEquals(3306, url.getHostAddresses().get(0).port);

        url = UrlParser.parse("jdbc:mysql://localhost:3307/test");
        assertEquals("localhost", url.getHostAddresses().get(0).host);
        assertEquals("test", url.getDatabase());
        assertEquals(3307, url.getHostAddresses().get(0).port);

    }

    @Test
    public void testEscapes() throws SQLException {
        String query = "select ?";
        PreparedStatement stmt = sharedConnection.prepareStatement(query);
        stmt.setString(1, "hej\"");
        ResultSet rs = stmt.executeQuery();
        assertEquals(true, rs.next());
        assertEquals(rs.getString(1), "hej\"");
    }

    @Test
    public void testPreparedWithNull() throws SQLException {
        String query = "select ? as test";
        PreparedStatement pstmt = sharedConnection.prepareStatement(query);
        pstmt.setNull(1, 1);
        ResultSet rs = pstmt.executeQuery();
        assertEquals(true, rs.next());
        assertEquals(null, rs.getString("test"));
        assertEquals(true, rs.wasNull());
    }


    @Test
    public void connectFailover() throws SQLException {
        String hosts = hostname + ":" + port + "," + hostname + ":" + (port + 1);
        String url = "jdbc:mysql://" + hosts + "/" + database + "?user=" + username;
        url += (password != null && !"".equals(password) ? "&password=" + password : "");
        Connection connection = null;
        try {
            connection = openNewConnection(url);
            MariaDbConnection my = (MariaDbConnection) connection;
            assertTrue(my.getPort() == port);
            ResultSet rs = connection.createStatement().executeQuery("select 1");
            rs.next();
            assertEquals(rs.getInt(1), 1);
        } finally {
            connection.close();
        }
    }

    @Test
    public void batchTest() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into test_batch values (null, ?)", 
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, "aaa");
        ps.addBatch();
        ps.setString(1, "bbb");
        ps.addBatch();
        ps.setString(1, "ccc");
        ps.addBatch();
        int[] batchResult = ps.executeBatch();
        ResultSet rs1 = ps.getGeneratedKeys();
        for (int count = 1; count <= 3; count++) {
            assertTrue(rs1.next());
            assertTrue(String.valueOf(count).equalsIgnoreCase(rs1.getString(1)));
        }
        for (int unitInsertNumber : batchResult) {
            assertEquals(1, unitInsertNumber);
        }
        ps.setString(1, "aaa");
        ps.addBatch();
        ps.setString(1, "bbb");
        ps.addBatch();
        ps.setString(1, "ccc");
        ps.addBatch();
        batchResult = ps.executeBatch();
        for (int unitInsertNumber : batchResult) {
            assertEquals(1, unitInsertNumber);
        }
        final ResultSet rs = sharedConnection.createStatement().executeQuery("select * from test_batch");
        ps.executeQuery("SELECT 1");
        rs1 = ps.getGeneratedKeys();
        assertEquals(MariaDbResultSet.EMPTY, rs1);
        assertEquals(true, rs.next());
        assertEquals("aaa", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("bbb", rs.getString(2));
        assertEquals(true, rs.next());
        assertEquals("ccc", rs.getString(2));

    }

    @Test
    public void batchTestStmt() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.addBatch("insert into test_batch2 values (null, 'hej1')");
        stmt.addBatch("insert into test_batch2 values (null, 'hej2')");
        stmt.addBatch("insert into test_batch2 values (null, 'hej3')");
        stmt.addBatch("insert into test_batch2 values (null, 'hej4')");
        stmt.executeBatch();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from test_batch2");
        for (int i = 1; i <= 4; i++) {
            assertEquals(true, rs.next());
            assertEquals(i, rs.getInt(1));
            assertEquals("hej" + i, rs.getString(2));
        }
        assertEquals(false, rs.next());

    }

    @Test
    public void floatingNumbersTest() throws SQLException {

        PreparedStatement ps = sharedConnection.prepareStatement("insert into test_float (a) values (?)");
        ps.setDouble(1, 3.99);
        ps.executeUpdate();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select a from test_float");
        assertEquals(true, rs.next());
        assertEquals((float) 3.99, rs.getFloat(1), 0.00001);
        assertEquals((float) 3.99, rs.getFloat("a"), 0.00001);
        assertEquals(false, rs.next());
    }


    @Test
    public void manyColumnsTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("drop table if exists test_many_columns");
        String query = "create table test_many_columns (a0 int primary key not null";
        for (int i = 1; i < 1000; i++) {
            query += ",a" + i + " int";
        }
        query += ")";
        stmt.execute(query);
        query = "insert into test_many_columns values (0";
        for (int i = 1; i < 1000; i++) {
            query += "," + i;
        }
        query += ")";
        stmt.execute(query);
        ResultSet rs = stmt.executeQuery("select * from test_many_columns");

        assertEquals(true, rs.next());

        for (int i = 0; i < 1000; i++) {
            assertEquals(rs.getInt("a" + i), i);
        }

    }

    @Test
    public void bigAutoIncTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("alter table test_big_autoinc2 auto_increment = 1000");
        stmt.execute("insert into test_big_autoinc2 values (null, 'hej')", Statement.RETURN_GENERATED_KEYS);
        ResultSet rsGen = stmt.getGeneratedKeys();
        assertEquals(true, rsGen.next());
        assertEquals(1000, rsGen.getInt(1));
        stmt.execute("alter table test_big_autoinc2 auto_increment = " + Short.MAX_VALUE);
        stmt.execute("insert into test_big_autoinc2 values (null, 'hej')");
        rsGen = stmt.getGeneratedKeys();
        assertEquals(true, rsGen.next());
        assertEquals(Short.MAX_VALUE, rsGen.getInt(1));
        stmt.execute("alter table test_big_autoinc2 auto_increment = " + Integer.MAX_VALUE);
        stmt.execute("insert into test_big_autoinc2 values (null, 'hej')");
        rsGen = stmt.getGeneratedKeys();
        assertEquals(true, rsGen.next());
        assertEquals(Integer.MAX_VALUE, rsGen.getInt(1));
    }

    @Test
    public void bigUpdateCountTest() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        for (int i = 0; i < 4000; i++) {
            stmt.execute("insert into test_big_update values (" + i + "," + i + ")");
        }
        ResultSet rs = stmt.executeQuery("select count(*) from test_big_update");
        assertEquals(true, rs.next());
        assertEquals(4000, rs.getInt(1));
        int updateCount = stmt.executeUpdate("update test_big_update set updateme=updateme+1");
        assertEquals(4000, updateCount);
    }


    @SuppressWarnings("deprecation")
    @Test
    public void testCharacterStreams() throws SQLException, IOException {
        PreparedStatement stmt = sharedConnection.prepareStatement(
                "insert into Driverstreamtest (id, strm) values (?,?)");
        stmt.setInt(1, 2);
        String toInsert = "abcdefgh\njklmn\"";
        Reader reader = new StringReader(toInsert);
        stmt.setCharacterStream(2, reader);
        stmt.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from Driverstreamtest");
        rs.next();
        Reader rdr = rs.getCharacterStream("strm");
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = rdr.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(sb.toString(), (toInsert));
        rdr = rs.getCharacterStream(2);
        sb = new StringBuilder();

        while ((ch = rdr.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(sb.toString(), (toInsert));
        InputStream is = rs.getAsciiStream("strm");
        sb = new StringBuilder();

        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(sb.toString(), (toInsert));
        is = rs.getUnicodeStream("strm");
        sb = new StringBuilder();

        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(sb.toString(), (toInsert));
    }

    @Test
    public void testCharacterStreamWithLength() throws SQLException, IOException {
        PreparedStatement stmt = sharedConnection.prepareStatement(
                "insert into Driverstreamtest2 (id, strm) values (?,?)");
        stmt.setInt(1, 2);
        String toInsert = "abcdefgh\njklmn\"";
        Reader reader = new StringReader(toInsert);
        stmt.setCharacterStream(2, reader, 5);
        stmt.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from Driverstreamtest2");
        rs.next();
        Reader rdr = rs.getCharacterStream("strm");
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = rdr.read()) != -1) {
            sb.append((char) ch);
        }
        assertEquals(sb.toString(), toInsert.substring(0, 5));
    }


    @Test
    public void testEmptyResultSet() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        assertEquals(true, stmt.execute("SELECT * FROM emptytest"));
        assertEquals(false, stmt.getResultSet().next());
    }

    @Test
    public void testLongColName() throws SQLException {
        DatabaseMetaData dbmd = sharedConnection.getMetaData();
        String str = "";
        for (int i = 0; i < dbmd.getMaxColumnNameLength(); i++) {
            str += "x";
        }
        createTable("longcol", str + " int not null primary key");
        sharedConnection.createStatement().execute("insert into longcol values (1)");
        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from longcol");
        assertEquals(true, rs.next());
        assertEquals(1, rs.getInt(1));
        assertEquals(1, rs.getInt(str));
    }

    @Test(expected = SQLException.class)
    public void testBadParamlist() throws SQLException {
        PreparedStatement ps = null;
        ps = sharedConnection.prepareStatement("insert into blah values (?)");
        ps.execute();
    }

    @Test
    public void setobjectTest() throws SQLException, IOException, ClassNotFoundException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into objecttest values (?,?,?,?)");
        ps.setObject(1, 5);
        ps.setObject(2, "aaa");
        ps.setObject(3, Timestamp.valueOf("2009-01-17 15:41:01"));
        ps.setObject(4, new SerializableClass("testing", 8));
        ps.execute();

        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from objecttest");
        assertEquals(true, rs.next());
        Object theInt = rs.getObject(1);
        assertTrue(theInt instanceof Integer);
        Object theInt2 = rs.getObject("int_test");
        assertTrue(theInt2 instanceof Integer);
        Object theString = rs.getObject(2);
        assertTrue(theString instanceof String);
        Object theTimestamp = rs.getObject(3);
        assertTrue(theTimestamp instanceof Timestamp);
        Object theBlob = rs.getObject(4);
        assertNotNull(theBlob);

        byte[] rawBytes = rs.getBytes(4);
        ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableClass sc = (SerializableClass) ois.readObject();

        assertEquals(sc.getVal(), "testing");
        assertEquals(sc.getVal2(), 8);
        rawBytes = rs.getBytes("serial_test");
        bais = new ByteArrayInputStream(rawBytes);
        ois = new ObjectInputStream(bais);
        sc = (SerializableClass) ois.readObject();

        assertEquals(sc.getVal(), "testing");
        assertEquals(sc.getVal2(), 8);
    }

    @Test
    public void binTest() throws SQLException, IOException {
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) (i & 0xff);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(allBytes);
        PreparedStatement ps = sharedConnection.prepareStatement("insert into bintest (bin1,bin2) values (?,?)");
        ps.setBytes(1, allBytes);
        ps.setBinaryStream(2, bais);
        ps.execute();

        ResultSet rs = sharedConnection.createStatement().executeQuery("select bin1,bin2 from bintest");
        assertTrue(rs.next());
        rs.getBlob(1);
        InputStream is = rs.getBinaryStream(1);

        for (int i = 0; i < 256; i++) {
            int read = is.read();
            assertEquals(i, read);
        }
        is = rs.getBinaryStream(2);

        for (int i = 0; i < 256; i++) {
            int read = is.read();
            assertEquals(i, read);
        }

    }

    /*
     Test with different APIs that generated keys work. Also test that any name in generatedKeys.getXXX(String name)
     can be passed and is equivalent to generatedKeys.getXXX(1). This might not be 100% compliant, but is a simple
     and effective solution for MySQL that does not does not support more than a single autogenerated value.
    */
    @Test
    public void generatedKeys() throws Exception {
        Statement st = sharedConnection.createStatement();
        st.executeUpdate("insert into genkeys(dataField) values('a')", Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = st.getGeneratedKeys();
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 1);
        assertEquals(rs.getInt("priKey"), 1);
        assertEquals(rs.getInt("foo"), 1);
        int[] indexes = {1, 2, 3};
        st.executeUpdate("insert into genkeys(dataField) values('b')", indexes);
        rs = st.getGeneratedKeys();
        assertTrue(rs.next());
        assertEquals(rs.getInt(1), 2);
        try {
            assertEquals(rs.getInt(2), 2);
            assertFalse("should never get here", true);
        } catch (SQLException e) {
            // eat
        }

        String[] columnNames = {"priKey", "Alice", "Bob"};
        st.executeUpdate("insert into genkeys(dataField) values('c')", columnNames);
        rs = st.getGeneratedKeys();
        assertTrue(rs.next());
        for (int i = 0; i < 3; i++) {
            assertEquals(rs.getInt(columnNames[i]), 3);
        }
    }

    @Test
    public void binTest2() throws SQLException, IOException {


        if (sharedConnection.getMetaData().getDatabaseProductName().toLowerCase().equals("mysql")) {
            createTable("bintest2", "bin1 longblob", "engine=innodb");
        } else {
            createTable("bintest2", "id int not null primary key auto_increment, bin1 blob");
        }

        byte[] buf = new byte[1000000];
        for (int i = 0; i < 1000000; i++) {
            buf[i] = (byte) i;
        }
        InputStream is = new ByteArrayInputStream(buf);
        PreparedStatement ps = sharedConnection.prepareStatement("insert into bintest2 (bin1) values (?)");
        ps.setBinaryStream(1, is);
        ps.execute();
        ps = sharedConnection.prepareStatement("insert into bintest2 (bin1) values (?)");
        is = new ByteArrayInputStream(buf);
        ps.setBinaryStream(1, is);
        ps.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select bin1 from bintest2");
        assertEquals(true, rs.next());
        byte[] buf2 = rs.getBytes(1);
        for (int i = 0; i < 1000000; i++) {
            assertEquals((byte) i, buf2[i]);
        }

        assertEquals(true, rs.next());
        buf2 = rs.getBytes(1);
        for (int i = 0; i < 1000000; i++) {
            assertEquals((byte) i, buf2[i]);
        }
        assertEquals(false, rs.next());
    }

    @Test(expected = SQLIntegrityConstraintViolationException.class)
    public void testException1() throws SQLException {
        sharedConnection.createStatement().execute("insert into extest values (1)");
        sharedConnection.createStatement().execute("insert into extest values (1)");
    }

    @Test
    public void testExceptionDivByZero() throws SQLException {
        ResultSet rs = sharedConnection.createStatement().executeQuery("select 1/0");
        assertEquals(rs.next(), true);
        assertEquals(null, rs.getString(1));
    }

    @Test(expected = SQLSyntaxErrorException.class)
    public void testSyntaxError() throws SQLException {
        sharedConnection.createStatement().executeQuery("create asdf b");
    }

    @Test
    public void testBatchLoop() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into rewritetest values (?,?,?)");
        for (int i = 0; i < 10; i++) {
            ps.setInt(1, i);
            ps.setString(2, "bbb" + i);
            ps.setInt(3, 30 + i);
            ps.addBatch();
        }
        ps.executeBatch();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from rewritetest");
        int counter = 0;
        while (rs.next()) {
            assertEquals(counter++, rs.getInt("id"));
        }
        assertEquals(10, counter);
    }

    @Test
    public void testBatchLoopWithDupKey() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement(
                "insert into rewritetest2 values (?,?,?) on duplicate key update a=values(a)");
        for (int i = 0; i < 2; i++) {
            ps.setInt(1, 0);
            ps.setString(2, "bbb" + i);
            ps.setInt(3, 30 + i);
            ps.addBatch();
        }
        ps.executeBatch();

        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from rewritetest2");
        int counter = 0;
        while (rs.next()) {
            assertEquals(counter++, rs.getInt("id"));
        }
        assertEquals(1, counter);
    }

    @Test
    public void testPreparedStatementsWithComments() throws SQLException {
        String query = "INSERT INTO commentPreparedStatements (a) VALUES (?) # ?";
        PreparedStatement pstmt = sharedConnection.prepareStatement(query);
        pstmt.setString(1, "yeah");
        pstmt.execute();
    }

    @Test
    public void testPreparedStatementsWithQuotes() throws SQLException {
        String query = "INSERT INTO quotesPreparedStatements (a,b) VALUES ('hellooo?', ?) # ?";
        PreparedStatement pstmt = sharedConnection.prepareStatement(query);
        pstmt.setString(1, "ff");
        pstmt.execute();
    }

    @Test
    public void bigDecimalTest() throws SQLException {
        requireMinimumVersion(5, 0);
        BigDecimal bd = BigDecimal.TEN;
        PreparedStatement ps = sharedConnection.prepareStatement("insert into bigdectest (bd) values (?)");
        ps.setBigDecimal(1, bd);
        ps.execute();

        ResultSet rs = sharedConnection.createStatement().executeQuery("select bd from bigdectest");
        assertTrue(rs.next());
        Object bb = rs.getObject(1);
        assertEquals(bd, bb);
        BigDecimal bigD = rs.getBigDecimal(1);
        BigDecimal bigD2 = rs.getBigDecimal("bd");
        assertEquals(bd, bigD);
        assertEquals(bd, bigD2);
        bigD = rs.getBigDecimal("bd");
        assertEquals(bd, bigD);
    }


    @Test
    public void byteTest() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into bytetest (a) values (?)");
        ps.setByte(1, Byte.MAX_VALUE);
        ps.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select a from bytetest");
        assertTrue(rs.next());

        Byte bc = rs.getByte(1);
        Byte bc2 = rs.getByte("a");

        assertTrue(Byte.MAX_VALUE == bc);
        assertEquals(bc2, bc);


    }


    @Test
    public void shortTest() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into shorttest (a) values (?)");
        ps.setShort(1, Short.MAX_VALUE);
        ps.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select a from shorttest");
        assertTrue(rs.next());

        Short bc = rs.getShort(1);
        Short bc2 = rs.getShort("a");

        assertTrue(Short.MAX_VALUE == bc);
        assertEquals(bc2, bc);


    }

    @Test
    public void doubleTest() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into doubletest (a) values (?)");
        double sendDoubleValue = 1.5;
        ps.setDouble(1, sendDoubleValue);
        ps.execute();
        ResultSet rs = sharedConnection.createStatement().executeQuery("select a from doubletest");
        assertTrue(rs.next());
        Object returnObject = rs.getObject(1);
        assertEquals(returnObject.getClass(), Double.class);
        Double bc = rs.getDouble(1);
        Double bc2 = rs.getDouble("a");

        assertTrue(sendDoubleValue == bc);
        assertEquals(bc2, bc);


    }


    @Test
    public void testResultSetPositions() throws SQLException {
        sharedConnection.createStatement().execute("insert into ressetpos values (1),(2),(3),(4)");

        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from ressetpos");
        assertTrue(rs.isBeforeFirst());
        rs.next();
        assertTrue(!rs.isBeforeFirst());
        assertTrue(rs.isFirst());
        rs.beforeFirst();
        assertTrue(rs.isBeforeFirst());
        while (rs.next()){
            //just load datas.
        }
        assertTrue(rs.isAfterLast());
        rs.absolute(4);
        assertTrue(!rs.isAfterLast());
        rs.absolute(2);
        assertEquals(2, rs.getInt(1));
        rs.relative(2);
        assertEquals(4, rs.getInt(1));
        assertTrue(!rs.next());
        rs.previous();
        assertEquals(4, rs.getInt(1));
        rs.relative(-3);
        assertEquals(1, rs.getInt(1));
        assertEquals(false, rs.relative(-1));
        assertEquals(1, rs.getInt(1));
        rs.last();
        assertEquals(4, rs.getInt(1));
        assertEquals(4, rs.getRow());
        assertTrue(rs.isLast());
        rs.first();
        assertEquals(1, rs.getInt(1));
        assertEquals(1, rs.getRow());
        rs.absolute(-1);
        assertEquals(4, rs.getRow());
        assertEquals(4, rs.getInt(1));
    }

    @Test(expected = SQLException.class)
    public void findColumnTest() throws SQLException {
        ResultSet rs = sharedConnection.createStatement().executeQuery("select 1 as 'hej'");
        assertEquals(1, rs.findColumn("hej"));

        rs.findColumn("nope");

    }

    @Test
    public void getStatementTest() throws SQLException {
        Statement stmt1 = sharedConnection.createStatement();
        ResultSet rs = stmt1.executeQuery("select 1 as 'hej'");
        assertEquals(stmt1, rs.getStatement());
    }

    @Test
    public void getUrlTest() throws SQLException {
        ResultSet rs = sharedConnection.createStatement().executeQuery("select 'http://mariadb.org' as url");
        rs.next();
        URL url = rs.getURL(1);
        assertEquals("http://mariadb.org", url.toString());
        url = rs.getURL("url");
        assertEquals("http://mariadb.org", url.toString());

    }

    @Test(expected = SQLException.class)
    public void getUrlFailTest() throws SQLException {
        ResultSet rs = sharedConnection.createStatement().executeQuery("select 'asdf' as url");
        rs.next();
        URL url = rs.getURL(1);
        assertNotNull(url);


    }

    @Test(expected = SQLException.class)
    public void getUrlFailTest2() throws SQLException {
        ResultSet rs = sharedConnection.createStatement().executeQuery("select 'asdf' as url");
        rs.next();
        URL url = rs.getURL("url");
        assertNotNull(url);
    }

    @Test
    public void setNull() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert blabla VALUE (?)");
        ps.setString(1, null);
    }

    @Test
    public void testBug501452() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into bug501452 (id,value) values (?,?)");
        ps.setObject(1, 1);
        ps.setObject(2, "value for 1");
        ps.addBatch();

        ps.executeBatch();

        ps.setObject(1, 2);
        ps.setObject(2, "value for 2");
        ps.addBatch();

        ps.executeBatch();
    }

    @Test
    public void testAutocommit() throws SQLException {
        assertTrue(sharedConnection.getAutoCommit());
        sharedConnection.setAutoCommit(false);
        assertFalse(sharedConnection.getAutoCommit());
        
        /* Check that autocommit value "false" , that driver derives from server status flags
         * remains the same when EOF, ERROR or OK packet were received.
         */
        sharedConnection.createStatement().executeQuery("select 1");
        assertFalse(sharedConnection.getAutoCommit());
        sharedConnection.createStatement().execute("set @a=1");
        assertFalse(sharedConnection.getAutoCommit());
        try {
            sharedConnection.createStatement().execute("insert into nosuchtable values(1)");
        } catch (Exception e) {
            //eat exception
        }
        assertFalse(sharedConnection.getAutoCommit());
        ResultSet rs = sharedConnection.createStatement().executeQuery("select @@autocommit");
        rs.next();
        assertEquals(0, rs.getInt(1));


        sharedConnection.setAutoCommit(true);
        
        /* Check that autocommit value "true" , that driver derives from server status flags
         * remains the same when EOF, ERROR or OK packet were received.
         */
        assertTrue(sharedConnection.getAutoCommit());
        sharedConnection.createStatement().execute("set @a=1");
        assertTrue(sharedConnection.getAutoCommit());
        try {
            sharedConnection.createStatement().execute("insert into nosuchtable values(1)");
        } catch (Exception e) {
            //eat exception
        }
        assertTrue(sharedConnection.getAutoCommit());
        rs = sharedConnection.createStatement().executeQuery("select @@autocommit");
        rs.next();
        assertEquals(1, rs.getInt(1));
        
        /* Set autocommit value using Statement.execute */
        sharedConnection.createStatement().execute("set @@autocommit=0");
        assertFalse(sharedConnection.getAutoCommit());

        sharedConnection.createStatement().execute("set @@autocommit=1");
        assertTrue(sharedConnection.getAutoCommit());
        
        /* Use session variable to set autocommit to 0 */
        Connection connection = null;
        try {
            connection = setConnection("&sessionVariables=autocommit=0");
            assertFalse(connection.getAutoCommit());
            sharedConnection.setAutoCommit(true);
        } finally {
            connection.close();
        }
    }

    @Test
    public void testUpdateCount() throws SQLException {
        Statement stmt = sharedConnection.createStatement();
        stmt.execute("select 1");
        assertTrue(-1 == stmt.getUpdateCount());
    }

    @Test
    public void testSetObject() throws SQLException {
        PreparedStatement ps = sharedConnection.prepareStatement("insert into test_setobjectconv values (null, ?, ?)");
        ps.setObject(1, "2009-01-01 00:00:00", Types.TIMESTAMP);
        ps.setObject(2, "33", Types.DOUBLE);
        ps.execute();
    }

    @Test
    public void testBit() throws SQLException {
        PreparedStatement stmt = sharedConnection.prepareStatement("insert into bittest values(null, ?)");
        stmt.setBoolean(1, true);
        stmt.execute();
        stmt.setBoolean(1, false);
        stmt.execute();

        ResultSet rs = sharedConnection.createStatement().executeQuery("select * from bittest");
        Assert.assertTrue(rs.next());
        Assert.assertTrue(rs.getBoolean("b"));
        Assert.assertTrue(rs.next());
        assertFalse(rs.getBoolean("b"));
        assertFalse(rs.next());
    }

    @Test
    public void testConnectWithDb() throws SQLException {
        requireMinimumVersion(5, 0);
        try {
            sharedConnection.createStatement().executeUpdate("drop database test_testdrop");
        } catch (Exception e) {
            //eat exception
        }
        Connection connection = null;
        try {
            connection = setConnection("&createDatabaseIfNotExist=true", "test_testdrop");

            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getCatalogs();
            boolean foundDb = false;
            while (rs.next()) {
                if (rs.getString("table_cat").equals("test_testdrop")) {
                    foundDb = true;
                }
            }
            assertTrue(foundDb);
            sharedConnection.createStatement().executeUpdate("drop database test_testdrop");
        } finally {
            connection.close();
        }
    }

    @Test
    public void testError() throws SQLException {
        // check that maxAllowedPacket is big enough for the test
        Assume.assumeTrue(checkMaxAllowedPacket("testError"));

        Connection connection = null;
        try {
            connection = setConnection();
            ResultSet rs = connection.createStatement().executeQuery("select @@max_allowed_packet");
            rs.next();
            int maxAllowedPacket = rs.getInt(1);

            int selectSize = 9;
            int packetHeader = 4 + 2; //packet header + 2 escape slash beacause of string escape
            char[] arr = new char[16 * 1024 * 1024 - selectSize - packetHeader];
            Arrays.fill(arr, 'a');
            String request = "select '" + new String(arr) + "'";
            System.out.println("request size : " + (request.length()) + " / " + maxAllowedPacket);

            rs = connection.createStatement().executeQuery(request);
            rs.next();
            log.trace(String.valueOf(rs.getString(1).length()));
        } finally {
            connection.close();
        }
    }


    @Test
    public void streamingResult() throws SQLException {
        Statement st = sharedConnection.createStatement();

        for (int i = 0; i < 100; i++) {
            st.execute("insert into streamingtest values('aaaaaaaaaaaaaaaaaa')");
        }
        st.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = st.executeQuery("select * from streamingtest");
        rs.next();
        rs.close();
        Statement st2 = sharedConnection.createStatement();
        ResultSet rs2 = st2.executeQuery("select * from streamingtest");
        rs2.next();
        rs.close();
    }

    // Test if driver works with sql_mode= NO_BACKSLASH_ESCAPES
    @Test
    public void noBackslashEscapes() throws SQLException {
        requireMinimumVersion(5, 0);

        // super privilege is needed for this test
        Assume.assumeTrue(hasSuperPrivilege("NoBackslashEscapes"));

        Statement st = sharedConnection.createStatement();
        ResultSet rs = st.executeQuery("select @@global.sql_mode");
        rs.next();
        String originalSqlMode = rs.getString(1);
        st.execute("set @@global.sql_mode = '" + originalSqlMode + ",NO_BACKSLASH_ESCAPES'");

        try {
            Connection connection = setConnection();
            try {
                PreparedStatement preparedStatement =
                        connection.prepareStatement("insert into testBlob2(a) values(?)");
                byte[] bytes = new byte[255];
                for (byte i = -128; i < 127; i++) {
                    bytes[i + 128] = i;
                }
                MariaDbBlob blob = new MariaDbBlob(bytes);
                preparedStatement.setBlob(1, blob);
                int affectedRows = preparedStatement.executeUpdate();
                Assert.assertEquals(affectedRows, 1);
            } finally {
                connection.close();
            }
        } finally {
            st.execute("set @@global.sql_mode='" + originalSqlMode + "'");
        }
    }

    // Test if driver works with sql_mode= NO_BACKSLASH_ESCAPES
    @Test
    public void noBackslashEscapes2() throws SQLException {
        requireMinimumVersion(5, 0);

        // super privilege is needed for this test
        Assume.assumeTrue(hasSuperPrivilege("NoBackslashEscapes2"));

        Statement st = sharedConnection.createStatement();
        ResultSet rs = st.executeQuery("select @@global.sql_mode");
        rs.next();
        String originalSqlMode = rs.getString(1);
        st.execute("set @@global.sql_mode = '" + originalSqlMode + ",NO_BACKSLASH_ESCAPES'");

        try {
            Connection connection = setConnection();
            try {
                PreparedStatement preparedStatement =
                        connection.prepareStatement("insert into testString2(a) values(?)");
                preparedStatement.setString(1, "'\\");
                int affectedRows = preparedStatement.executeUpdate();
                Assert.assertEquals(affectedRows, 1);
                preparedStatement.close();
                preparedStatement =
                        connection.prepareStatement("select * from testString2");
                rs = preparedStatement.executeQuery();
                rs.next();
                String out = rs.getString(1);
                assertEquals(out, "'\\");
                Statement st2 = connection.createStatement();
                rs = st2.executeQuery("select 'a\\b\\c'");
                rs.next();
                assertEquals("a\\b\\c", rs.getString(1));

            } finally {
                connection.close();
            }
        } finally {
            st.execute("set @@global.sql_mode='" + originalSqlMode + "'");
        }
    }

    // Test if driver works with sql_mode= ANSI_QUOTES
    @Test
    public void ansiQuotes() throws SQLException {

        // super privilege is needed for this test
        Assume.assumeTrue(hasSuperPrivilege("AnsiQuotes"));

        Statement st = sharedConnection.createStatement();
        ResultSet rs = st.executeQuery("select @@global.sql_mode");
        rs.next();
        String originalSqlMode = rs.getString(1);
        st.execute("set @@global.sql_mode = '" + originalSqlMode + ",ANSI_QUOTES'");

        try {
            Connection connection = setConnection();
            try {
                PreparedStatement preparedStatement =
                        connection.prepareStatement("insert into testBlob2(a) values(?)");
                byte[] bytes = new byte[255];
                for (byte i = -128; i < 127; i++) {
                    bytes[i + 128] = i;
                }
                MariaDbBlob blob = new MariaDbBlob(bytes);
                preparedStatement.setBlob(1, blob);
                int affectedRows = preparedStatement.executeUpdate();
                Assert.assertEquals(affectedRows, 1);
            } finally {
                connection.close();
            }
        } finally {
            st.execute("set @@global.sql_mode='" + originalSqlMode + "'");
        }
    }

    @Test
    public void unsignedTest() throws Exception {
        Statement st = sharedConnection.createStatement();
        st.execute("insert into unsignedtest values(4294967295)");
        ResultSet rs = st.executeQuery("select * from unsignedtest");
        rs.next();
        assertNotNull(rs.getLong("unsignedtest.a"));
    }


    @Test
    public void useSsl() throws Exception {
        Assume.assumeTrue(haveSsl(sharedConnection));
        //Skip SSL test on java 7 since SSL packet size JDK-6521495).
        org.junit.Assume.assumeFalse(System.getProperty("java.version").contains("1.7."));
        Connection connection = setConnection("&useSSL=true&trustServerCertificate=true");
        try {
            connection.createStatement().execute("select 1");
        } finally {
            connection.close();
        }
    }

    @Test
    // Bug in URL parser
    public void mdev3916() throws Exception {
        try {
            setConnection("&password=");
        } catch (SQLException ex) {
            //SQLException is ok because we might get for example an access denied exception
            if (!(ex.getMessage().indexOf("Could not connect: Access denied") > -1)) {
                throw ex;
            }
        }
    }

    @Test
    public void conj1() throws Exception {
        requireMinimumVersion(5, 0);

        Connection connection = setConnection();
        try {
            Statement st = connection.createStatement();
            st.setQueryTimeout(1);
            st.execute("select sleep(0.5)");
            try {
                st.execute("select sleep(1.5)");
                assertFalse("must be exception here", true);
            } catch (Exception e) {
                //normal exception
            }

            Statement st2 = connection.createStatement();
            assertEquals(st2.getQueryTimeout(), 0);
            // no exception
            st2.execute("select sleep(1.5)");

            Statement st3 = connection.createStatement();
            st3.setQueryTimeout(1);
            st3.execute("select sleep(0.1)");
            assertEquals(st3.getQueryTimeout(), 1);
        } finally {
            connection.close();
        }
    }

    /* Check that exception contains SQL statement, for queries with syntax errors */
    @Test
    public void dumpQueryOnSyntaxException() throws Exception {
        String syntacticallyWrongQuery = "banana";
        try {
            Statement st = sharedConnection.createStatement();
            st.execute(syntacticallyWrongQuery);
        } catch (SQLException sqle) {
            assertTrue(sqle.getMessage().contains("Query is:\n" + syntacticallyWrongQuery));
        }
    }

    /* Check that query contains SQL statement, if dumpQueryOnException is true */
    @Test
    public void dumpQueryOnException() throws Exception {
        Connection connection = null;
        try {
            connection = setConnection("&dumpQueriesOnException=true");
            String selectFromNonExistingTable = "select * from banana";
            try {
                Statement st = connection.createStatement();
                st.execute(selectFromNonExistingTable);
            } catch (SQLException sqle) {
                assertTrue(sqle.getMessage().contains("Query is:\n" + selectFromNonExistingTable));
            }
        } finally {
            connection.close();
        }
    }

    /* CONJ-14 
     * getUpdateCount(), getResultSet() should indicate "no more results" with
     * (getUpdateCount() == -1 && getResultSet() == null)  
     */
    @Test
    public void conj14() throws Exception {
        Statement st = sharedConnection.createStatement();
        
        /* 1. Test update statement */
        st.execute("use " + database);
        assertEquals(0, st.getUpdateCount());

        /* No more results */
        assertFalse(st.getMoreResults());
        assertEquals(-1, st.getUpdateCount());
        assertEquals(null, st.getResultSet());
        
        /* 2. Test select statement */
        st.execute("select 1");
        assertEquals(-1, st.getUpdateCount());
        assertTrue(st.getResultSet() != null);
        
        /* No More results */
        assertFalse(st.getMoreResults());
        assertEquals(-1, st.getUpdateCount());
        assertEquals(null, st.getResultSet());
        
        /* Test batch  */
        Connection connection = null;
        try {
            connection = setConnection("&allowMultiQueries=true");
            st = connection.createStatement();
            
            /* 3. Batch with two SELECTs */

            st.execute("select 1;select 2");
            /* First result (select)*/
            assertEquals(-1, st.getUpdateCount());
            assertTrue(st.getResultSet() != null);
            
            /* has more results */
            assertTrue(st.getMoreResults()); 
            
            /* Second result (select) */
            assertEquals(-1, st.getUpdateCount());
            assertTrue(st.getResultSet() != null);
            
            /* no more results */
            assertFalse(st.getMoreResults());
            assertEquals(-1, st.getUpdateCount());
            assertEquals(null, st.getResultSet());
            
            /* 4. Batch with a SELECT and non-SELECT */

            st.execute("select 1; use " + database);
            /* First result (select)*/
            assertEquals(-1, st.getUpdateCount());
            assertTrue(st.getResultSet() != null);
            
            /* has more results */
            assertTrue(st.getMoreResults()); 
            
            /* Second result (use) */
            assertEquals(0, st.getUpdateCount());
            assertTrue(st.getResultSet() == null);
            
            /* no more results */
            assertFalse(st.getMoreResults());
            assertEquals(-1, st.getUpdateCount());
            assertEquals(null, st.getResultSet());
        } finally {
            connection.close();
        }
    }

    @Test
    public void conj25() throws Exception {
        Statement stmt;
        stmt = sharedConnection.createStatement();
        String st = "INSERT INTO conj25 VALUES (REPEAT('a',1024))";
        for (int i = 1; i <= 100; i++) {
            st = st + ",(REPEAT('a',1024))";
        }
        stmt.setFetchSize(Integer.MIN_VALUE);
        stmt.execute(st);
        stmt.executeQuery("SELECT * FROM conj25 a, conj25 b");

    }

    @Test
    public void namedpipe() throws Exception {
        String namedPipeName = null;
        try {
            ResultSet rs = sharedConnection.createStatement().executeQuery("select @@named_pipe,@@socket");
            rs.next();
            if (rs.getBoolean(1)) {
                namedPipeName = rs.getString(2);
            } else {
                log.info("test 'namedpipe' skipped");
            }
        } catch (SQLException e) {
            //named pipe not found,
            log.info("test 'namedpipe' skipped");
        }

        //skip test if no namedPipeName was obtained because then we do not use a socket connection
        Assume.assumeTrue(namedPipeName != null);

        Connection connection = null;
        try {
            connection = setConnection("&pipe=" + namedPipeName);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            assertTrue(rs.next());
            rs.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void batchUpdateException() throws Exception {
        Statement st = sharedConnection.createStatement();
        st.addBatch("insert into batchUpdateException values(1)");
        st.addBatch("insert into batchUpdateException values(2)");
        st.addBatch("insert into batchUpdateException values(1)"); // will fail, duplicate primary key


        try {
            st.executeBatch();
            fail("exception should be throw above");
        } catch (BatchUpdateException bue) {
            int[] updateCounts = bue.getUpdateCounts();
            assertEquals(2, updateCounts.length);
            assertEquals(1, updateCounts[0]);
            assertEquals(1, updateCounts[1]);
            assertTrue(bue.getCause() instanceof SQLIntegrityConstraintViolationException);
        }
    }

    @Test
    public void localSocket() throws Exception {
        requireMinimumVersion(5, 1);

        Assume.assumeTrue(isLocalConnection("localSocket"));

        Statement st = sharedConnection.createStatement();
        ResultSet rs = st.executeQuery("select @@version_compile_os,@@socket");
        if (!rs.next()) {
            return;
        }
        log.info("os:" + rs.getString(1) + " path:" + rs.getString(2));
        String os = rs.getString(1);
        if (os.toLowerCase().startsWith("win")) {
            return;
        }

        String path = rs.getString(2);
        Connection connection = null;
        try {
            connection = setConnection("&localSocket=" + path);
            rs = connection.createStatement().executeQuery("select 1");
            rs.next();
        } finally {
            connection.close();
        }
    }

    @Test
    public void sharedMemory() throws Exception {
        requireMinimumVersion(5, 1);
        Statement st = sharedConnection.createStatement();
        ResultSet rs = st.executeQuery("select @@version_compile_os");
        if (!rs.next()) {
            return;
        }

        String os = rs.getString(1);
        if (!os.toLowerCase().startsWith("win")) {
            return;  // skip test on non-Windows
        }

        rs = st.executeQuery("select @@shared_memory,@@shared_memory_base_name");
        if (!rs.next()) {
            return;
        }

        if (!rs.getString(1).equals("1")) {
            return;
        }

        String shmBaseName = rs.getString(2);
        Connection connection = null;
        try {
            connection = setConnection("&sharedMemory=" + shmBaseName);
            rs = connection.createStatement().executeQuery("select repeat('a',100000)");
            rs.next();
            assertEquals(100000, rs.getString(1).length());
            char[] arr = new char[100000];
            Arrays.fill(arr, 'a');
            rs = connection.createStatement().executeQuery("select '" + new String(arr) + "'");
            rs.next();
            assertEquals(100000, rs.getString(1).length());
        } finally {
            connection.close();
        }
    }

    @Test
    public void preparedStatementToString() throws Exception {
        PreparedStatement ps = sharedConnection.prepareStatement("SELECT ?,?,?,?,?,?");
        ps.setInt(1, 1);
        ps.setBigDecimal(2, new BigDecimal("1"));
        ps.setString(3, "one");
        ps.setBoolean(4, true);
        Calendar calendar = new GregorianCalendar(1972, 3, 22);
        ps.setDate(5, new java.sql.Date(calendar.getTime().getTime()));
        ps.setDouble(6, 1.5);
        assertEquals("sql : 'SELECT ?,?,?,?,?,?', parameters : [1,1,'one',1,'1972-04-22',1.5]", ps.toString());
        ps.close();
    }


    /* Test that CLOSE_CURSORS_ON_COMMIT is silently ignored, and HOLD_CURSORS_OVER_COMMIT is actually used*/
    @Test
    public void resultSetHoldability() throws Exception {
        Statement st = sharedConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, st.getResultSetHoldability());
        PreparedStatement ps = sharedConnection.prepareStatement("SELECT 1", ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, ps.getResultSetHoldability());
        ResultSet rs = ps.executeQuery();
        assertEquals(rs.getHoldability(), ResultSet.HOLD_CURSORS_OVER_COMMIT);
        CallableStatement cs = sharedConnection.prepareCall("{CALL foo}", ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        assertEquals(cs.getResultSetHoldability(), ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    @Test
    public void emptyBatch() throws Exception {
        Statement st = sharedConnection.createStatement();
        st.executeBatch();
    }

    @Test
    public void createDbWithSpacesTest() throws SQLException {
        Connection connection = null;
        try {
            connection = setConnection("&createDatabaseIfNotExist=true", "test with spaces");
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet rs = dbmd.getCatalogs();
            boolean foundDb = false;
            while (rs.next()) {
                if (rs.getString("table_cat").equals("test with spaces")) {
                    foundDb = true;
                }
            }
            assertTrue(foundDb);
            connection.createStatement().execute("drop database `test with spaces`");
        } finally {
            connection.close();
        }
    }
}
