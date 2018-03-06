package com.ef;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Java program that parses web server access log file, loads the log to MySQL 
 * and checks if a given IP makes more than a certain number of requests for the given duration.
 * @author AMD
 * @see https://github.com/amdelamar/parser
 * @version 1.0.0
 */
public class Parser {

    public static final String DB_PROPERTIES = "/db.properties";

    public static Arguments args;
    public static Database db;

    /**
     * Main program
     * @param s array of arguments
     */
    public static void main(String[] s) {

        try {
            // read in arguments
            args = parseArguments(s);

            // read db.properties
            DatabaseLoader dbloader = new DatabaseLoader();
            db = dbloader.readProperties(DB_PROPERTIES);

            // setup database connection
            dbloader.checkDriver();
            Connection conn = dbloader.getConnection(db);

            if (args.getAccesslog() != null) {
                // upload access log file
                System.out.println("Uploading access log...");
                dbloader.uploadAccessLog(conn, args.getAccesslog());
                System.out.println("Done.");
            } else {
                // check if valid 
                validateArguments(args);
                // query table
                dbloader.queryTable(conn, args.getStartDate(), args.getDuration(), args.getThreshold());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Parses the command line arguments and stores them into the global variables.
     * @param args String array
     * @return Arguments
     * @throws IllegalArgumentException if arguments are missing or invalid
     */
    public static Arguments parseArguments(String[] args) throws IllegalArgumentException {
        if (args.length <= 0) {
            throw new IllegalArgumentException("usage: [--startDate=] [--duration=] [--threshold=] ([--accesslog=])");
        }
        Arguments arguments = new Arguments();

        // parse each argument
        for (int i = 0; i < args.length; i++) {
            // split name from value
            String arg = args[i].substring(0, args[i].indexOf("="));
            String value = args[i].substring(args[i].indexOf("=") + 1, args[i].length());

            switch (arg) {
            case "--startDate":
                arguments.setStartDate(value);
                break;
            case "--duration":
                arguments.setDuration(value);
                break;
            case "--threshold":
                arguments.setThreshold(Integer.parseInt(value));
                break;
            case "--accesslog":
                arguments.setAccesslog(value);
                break;
            default:
                throw new IllegalArgumentException(arg + " is not recognized as a valid argument.");
            }
        }
        return arguments;
    }

    /**
     * Check the business rules for each argument validation.
     * @param args
     * @return true if valid
     * @throws IllegalArgumentException if arguments are invalid
     */
    public static boolean validateArguments(Arguments args) {

        if (args.getStartDate() == null || args.getStartDate()
                .isEmpty()) {
            throw new IllegalArgumentException("startDate cannot be empty.");
        }

        if (args.getDuration() == null || args.getDuration()
                .isEmpty()) {
            throw new IllegalArgumentException("duration cannot be empty.");
        }

        if (args.getThreshold() < 0) {
            throw new IllegalArgumentException("threshold cannot be a negative number.");
        }

        switch (args.getDuration()) {
        case "daily":
            break;
        case "hourly":
            break;
        default:
            throw new IllegalArgumentException("duration must be 'daily' or 'hourly' only.");
        }

        try {
            @SuppressWarnings("unused")
            Date date = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").parse(args.getStartDate());
        } catch (ParseException e) {
            throw new IllegalArgumentException("startDate is invalid. Expected 'yyyy-MM-dd.HH:mm:ss' format.");
        }
        // all else, is good
        return true;
    }
}
