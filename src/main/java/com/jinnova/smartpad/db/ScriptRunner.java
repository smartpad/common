package com.jinnova.smartpad.db;

/*
 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 */
/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tool to run database scripts
 */
public class ScriptRunner {

    private static final String DEFAULT_DELIMITER = ";";

    private Connection connection;

    private boolean stopOnError;
    private boolean autoCommit;

    private PrintWriter logWriter = new PrintWriter(System.out);
    private PrintWriter errorLogWriter = new PrintWriter(System.err);

    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

	public static void createDatabase(String dbhost, String dbport, String dbname, String dblogin, String dbpass, boolean withDrilling) throws SQLException, FileNotFoundException, IOException {
		createDatabase(dbhost, dbport, dbname, dblogin, dbpass);
		if (withDrilling) {
			Connection conn = DriverManager.getConnection(makeDburl(dbhost, dbport, dbname), dblogin, dbpass);
			System.out.println("Working dir: " + new File(".").getAbsolutePath());
			ScriptRunner runner = new ScriptRunner(conn, true, true);
			runner.runScript(new FileReader("../common/src/main/sql/schema_drill.sql"));
			conn.close();
		}
	}
	
	public static String makeDburl(String dbhost, String dbport, String dbname) {
		String dburl = "jdbc:mysql://" + dbhost;
		if (dbport != null) {
			dburl = dburl + ":" + dbport;
		}
		return dburl + "/" + dbname + "?useUnicode=true&characterEncoding=UTF-8";
	}

	private static void createDatabase(String dbhost, String dbport, String dbname, String dblogin, String dbpass) throws SQLException, FileNotFoundException, IOException {

		String dburl = "jdbc:mysql://" + dbhost;
		if (dbport != null) {
			dburl = dburl + ":" + dbport;
		}
		Connection conn = DriverManager.getConnection(makeDburl(dbhost, dbport, "mysql"), dblogin, dbpass);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("CREATE DATABASE " + dbname + " DEFAULT CHARACTER SET utf8");
		stmt.close();
		conn.close();

		conn = DriverManager.getConnection(dburl + "/" + dbname, dblogin, dbpass);
		System.out.println("Working dir: " + new File(".").getAbsolutePath());
		ScriptRunner runner = new ScriptRunner(conn, true, true);
		runner.runScript(new FileReader("../common/src/main/sql/schema.sql"));

		runner = new ScriptRunner(conn, true, true);
		runner.setDelimiter("$$", false);
		runner.runScript(new FileReader("../common/src/main/sql/stored_routines.sql"));
		/**
		 * select sp_dist(10.780000, 106.681000, 10.780000, 106.682000), 
			sp_dist_grade(10.780000, 106.681000, 10.780000, 106.682000),
			sp_dist_grade(10.780000, 106.680000, 10.780000, 106.690000),
			
			sp_dist_grade(10.780000, 106.680000, 10.780000, 206.690000)
		 */
		conn.close();
	}

    /**
     * Default constructor
     */
    public ScriptRunner(Connection connection, boolean autoCommit,  boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Setter for logWriter property
     *
     * @param logWriter
     *            - the new value of the logWriter property
     */
    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Setter for errorLogWriter property
     *
     * @param errorLogWriter
     *            - the new value of the errorLogWriter property
     */
    public void setErrorLogWriter(PrintWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader
     *            - the source of the script
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn
     *            - the connection to use for the script
     * @param reader
     *            - the source of the script
     * @throws SQLException
     *             if any SQL errors occur
     * @throws IOException
     *             if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    println(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {
                    command.append(line.substring(0, line
                            .lastIndexOf(getDelimiter())));
                    command.append(" ");
                    Statement statement = conn.createStatement();

                    println(command);

                    boolean hasResults = false;
                    if (stopOnError) {
                        hasResults = statement.execute(command.toString());
                    } else {
                        try {
                            statement.execute(command.toString());
                        } catch (SQLException e) {
                            e.fillInStackTrace();
                            printlnError("Error executing: " + command);
                            printlnError(e);
                        }
                    }

                    if (autoCommit && !conn.getAutoCommit()) {
                        conn.commit();
                    }

                    ResultSet rs = statement.getResultSet();
                    if (hasResults && rs != null) {
                        ResultSetMetaData md = rs.getMetaData();
                        int cols = md.getColumnCount();
                        for (int i = 0; i < cols; i++) {
                            String name = md.getColumnLabel(i);
                            print(name + "\t");
                        }
                        println("");
                        while (rs.next()) {
                            for (int i = 0; i < cols; i++) {
                                String value = rs.getString(i);
                                print(value + "\t");
                            }
                            println("");
                        }
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        // Ignore to workaround a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } finally {
        	if (!autoCommit) {
        		conn.rollback();
        	}
            flush();
        }
    }

    private String getDelimiter() {
        return delimiter;
    }

    private void print(Object o) {
        if (logWriter != null) {
            System.out.print(o);
        }
    }

    private void println(Object o) {
        if (logWriter != null) {
            logWriter.println(o);
        }
    }

    private void printlnError(Object o) {
        if (errorLogWriter != null) {
            errorLogWriter.println(o);
        }
    }

    private void flush() {
        if (logWriter != null) {
            logWriter.flush();
        }
        if (errorLogWriter != null) {
            errorLogWriter.flush();
        }
    }
}
