package com.ef;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Reads database properties from file and uploads the access.log into the specified table.
 * @author AMD
 */
public class DatabaseLoader {

    public static final String INSERT = "insert into accesslog (date,ip,request,status,useragent) values (?,?,?,?,?)";
    public static final String QUERY = "select ip, count(*) as count from accesslog where date >= ? and date < ? group by ip having count(*) > ? order by COUNT(*) desc";

    /**
     * Reads properties file for host, port, username, and password.
     * @param file
     * @throws IOException if file not found or corrupt
     */
    public Database readProperties(String file) throws IOException {
        Properties properties = new Properties();
        properties.load(DatabaseLoader.class.getResourceAsStream(file));

        Database db = new Database();
        db.setHost(properties.getProperty("host"));
        db.setPort(properties.getProperty("port"));
        db.setDatabase(properties.getProperty("database"));
        db.setUsername(properties.getProperty("username"));
        db.setPassword(properties.getProperty("password"));
        return db;
    }

    /**
     * Attempts and returns a database connection object.
     * @param db
     * @return Connection
     * @throws SQLException if invalid connection
     */
    public Connection getConnection(Database db) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + db.getHost() + ":" + db.getPort() + "/" + db.getDatabase(),
                db.getUsername(), db.getPassword());
    }

    /**
     * Checks for the MySQL JDBC Driver
     * @return true if found
     * @throws ClassNotFoundException if not found
     */
    public boolean checkDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return true;
    }

    /**
     * Reads the access log file, and inserts each line into the database table.
     * **NOTE:** Large files will take a long time. An enhancement to this function
     * would be to give a progress bar or some output percent % complete.
     * @param conn
     * @param file
     * @throws SQLException
     * @throws IOException
     */
    public void uploadAccessLog(Connection conn, String file) throws SQLException, IOException {
        try (BufferedReader buffr = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = buffr.readLine()) != null) {
                String[] temp = line.split("\\|");
                PreparedStatement psmt = conn.prepareStatement(INSERT);
                psmt.setString(1, temp[0]);
                psmt.setString(2, temp[1]);
                psmt.setString(3, temp[2]);
                psmt.setInt(4, Integer.parseInt(temp[3]));
                psmt.setString(5, temp[4]);
                psmt.executeUpdate();
            }
        }
    }

    /**
     * Queries the database table with the given parameters.
     * @param conn
     * @param startDate
     * @param duration
     * @param threshold
     * @throws SQLException 
     */
    public void queryTable(Connection conn, String startDate, String duration, int threshold) throws SQLException {

        // configure timestamp range
        String nextHour = null, nextDay = null;
        try {
            Date day = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").parse(startDate);
            Timestamp ts = new Timestamp(day.getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(ts.getTime());

            // add 1 hour
            DateFormat dateform = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            cal.add(Calendar.HOUR, 1);
            nextHour = dateform.format(cal.getTime());

            // add 23 hours
            cal.add(Calendar.HOUR, 23);
            nextDay = dateform.format(cal.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // prepare query
        PreparedStatement psmt = conn.prepareStatement(QUERY);
        psmt.setString(1, startDate);
        if ("daily".equals(duration)) {
            psmt.setString(2, nextDay);
        } else {
            psmt.setString(2, nextHour);
        }
        psmt.setInt(3, threshold);

        // test print System.out.println(psmt);

        // execute query
        if (psmt.execute()) {
            ResultSet rs = psmt.getResultSet();

            // print results
            if (rs.first()) {
                System.out.println(rs.getString(2) + " requests from " + rs.getString(1));
                while (rs.next()) {
                    System.out.println(rs.getString(2) + " requests from " + rs.getString(1));
                }
            } else {
                System.err.println("No results found.");
            }

        } else {
            System.err.println("Error running query.");
        }
    }
}
