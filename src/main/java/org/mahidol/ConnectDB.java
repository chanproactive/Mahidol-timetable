package org.mahidol;

import java.sql.*;
import java.util.ArrayList;

public class ConnectDB {
    static Connection conn;


    public ConnectDB() throws Exception {
    }

    public static Connection getConnection() throws Exception {
       Class.forName( "org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:database\\subject_data.db");
    }

    public void createNewTable(String tableName) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS \""+tableName+"\" (\n" +
                "\t\"parameter\"\tTEXT,\n" +
                "\t\"code\"\tTEXT,\n" +
                "\t\"course\"\tTEXT,\n" +
                "\t\"credits\"\tINTEGER,\n" +
                "\t\"startTime\"\tINTEGER,\n" +
                "\t\"finishTime\"\tINTEGER,\n" +
                "\t\"room\"\tTEXT,\n" +
                "\t\"day\"\tINTEGER,\n" +
                "\t\"details\"\tTEXT,\n" +
                ")";
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        statement.execute(sql);
        conn.close();
    }


    public void insertDB(SubjectData sd,String tableName, String parameter) throws Exception {
        String sql = "INSERT INTO "+tableName+" (parameter,code, course,credits,startTime,finishTime,room,day,details)" +
                " VALUES(?,?,?,?,?,?,?,?,?)";
        Connection conn =  getConnection();
        PreparedStatement pStatement = conn.prepareStatement(sql);
        pStatement.setString(1,parameter);
        pStatement.setString(2,sd.getCode());
        pStatement.setString(3,sd.getCourse());
        pStatement.setInt(4,sd.getCredits());
        pStatement.setInt(5,sd.getStartTime());
        pStatement.setInt(6,sd.getFinishTime());
        pStatement.setString(7,sd.getRoom());
        pStatement.setInt(8,sd.getDay());
        pStatement.setString(9,sd.getDetails());
        pStatement.executeUpdate();
        System.out.println("Data is written");
        conn.close();
    }


    public ResultSet readDB (String tableName,String parameter) throws Exception {
        conn =  getConnection();
        if(parameter.equals("")){
            String sql = "SELECT * FROM "+tableName;
            PreparedStatement pstmt  = conn.prepareStatement(sql);
            return pstmt.executeQuery();
        }
        else {String sql = "SELECT * FROM "+tableName+ " WHERE parameter = ?";
        PreparedStatement pstmt  = conn.prepareStatement(sql);
            pstmt.setString(1,parameter);

        return pstmt.executeQuery();
        }
    }
    public ResultSet getSubjectDataFromCode (String code) throws Exception {
        conn =  getConnection();

       String sql = "SELECT * FROM subject_data  WHERE code = ?";
            PreparedStatement pstmt  = conn.prepareStatement(sql);
            pstmt.setString(1,code);

            return pstmt.executeQuery();

    }

    public void delete(String tableName, String parameter) throws Exception {
        String sql = "DELETE FROM "+tableName+" WHERE parameter = ?";
        Connection conn =  getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,parameter);
        pstmt.executeUpdate();
        conn.close();
    }
    public void deleteAll(String tableName) throws Exception {
        String sql = "DELETE FROM "+tableName;
        Connection conn =  getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.executeUpdate();
        conn.close();
    }

    public Boolean checkIfTableExist(String tableName) throws Exception {
        conn =  getConnection();
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, null, tableName, null);
        conn.close();
        return rs.next();
    }

    //Handle UUID from google service
    public void saveUUID(String id) throws Exception {
        conn =  getConnection();
        Statement statement = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS \"UUID\" (\n" +
                "\t\"id\"\tTEXT UNIQUE\n" +
                ");";
        statement.execute(sql);
         sql = "INSERT INTO UUID (id) values(?)";
        PreparedStatement pStatement = conn.prepareStatement(sql);
        pStatement.setString(1,id);
        pStatement.executeUpdate();
         conn.close();
    }

    public ArrayList<String> deleteUUID() throws Exception {
        ArrayList<String> UUIDSet = new ArrayList<>();
        Connection conn =  getConnection();
        Statement statement = conn.createStatement();
        String sql = "SELECT * FROM UUID";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            UUIDSet.add(resultSet.getString("id"));
        }
        sql = "DELETE FROM UUID";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.executeUpdate();
        conn.close();
        return UUIDSet;
    }
}
