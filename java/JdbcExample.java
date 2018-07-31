package com.sap.scp.srch.sif.swa.reporting.more;

import java.sql.*;
import java.util.*;

public abstract class HanaDBHandler {
    protected Connection conn = null;
    private String sqlStatementFulltextAlt = "SELECT COUNT (*) AS total FROM $repo.$table WHERE (CONTAINS($column,'/*/',FUZZY(1))) OR $column IS NULL;";

    protected void connect(String hanaServer, String hanaInstance, String hanaUser, String hanaPassword) {
        try {
            // e.g. jdbc:sap://myhdb:30715/?autocommit=false",myname,mysecret :
            // example of connecting to an SAP HANA server called myhdb,
            // which was installed as instance 07, with user name myname and
            // password mysecret
            conn = DriverManager.getConnection(
                "jdbc:sap://" + hanaServer + ":3" + hanaInstance + "15/?autocommit=false", hanaUser, hanaPassword);
            // prerequisite is to add the JDBC driver (ngdbc.jar) to classpath

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            // System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    protected void disconnect() {
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                // System.exit(0);
            }
        }
    }

    protected int getNumColumns(ResultSet rs) {
        try {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();
            return numberOfColumns;
        } catch (SQLException e) {
            System.out.println("Error getting MetaData for ResultSet");
            return 0;
        }
    }

    protected void printList(List < String > list) {
        Iterator < String > it = list.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public abstract List < String > doSelect(String sql);

}

public class HanaIndexReader extends HanaDBHandler implements Tasklet {

    private String sqlStatementIndexes = "SELECT schema_name, table_name, column_name, indexed_document_count FROM \"PUBLIC\".\"M_FULLTEXT_QUEUES\";";
    private String sqlStatementFulltext = "SELECT COUNT (*) AS total FROM $repo.$table WHERE (CONTAINS($column,'/*/',FUZZY(1)));";

    HanaIndexReader reader = new HanaIndexReader();
    reader.setProdFlag(prodFlag);
    reader.connect(getHanaServer(), getHanaInstance(), getHanaUser(), getHanaPassword());
    List < String > results = reader.doSelect(sqlStatementFulltext);

}