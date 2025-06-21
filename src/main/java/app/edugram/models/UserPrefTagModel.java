package app.edugram.models;

import app.edugram.utils.Sessions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserPrefTagModel extends BaseModel implements Toggleable{
    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public boolean set(List<String> dbValue) {
        String query = "INSERT INTO userPrefTag (id_user, id_tag) VALUES (";
        return Toggleable.LoopValues(dbValue, query);
    }

    @Override
    public boolean unset(int tableId) {
        String sql = "DELETE FROM userPrefTag WHERE id_tag = " + tableId + " AND id_user = " + Sessions.getUserId();

        return ConnectDB.startQueryExecution(sql, true);
    }

    @Override
    public boolean exists(int tableId) {
        String query = "SELECT * FROM userPrefTag WHERE id_tag = " + tableId + " AND id_user = " + Sessions.getUserId();
        return ConnectDB.startQueryExecution(query, false);
    }
    public static String getTagId(String tagNama) {
        String sql = "SELECT id_tag FROM tag WHERE nama_tag = ?";
        ConnectDB db = new ConnectDB();

        try (Connection con = db.getConnetion();
            PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setString(1, tagNama);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("id_tag");
            }
        } catch (SQLException e) {
            System.err.println("Error getting tag id: " + e.getMessage());
        }

        return null; // tidak ditemukan
    }


    public static List<String> listAll(){
        System.out.println("fetching usr pref tag  " + Sessions.getUserId());
        String sql = "SELECT * FROM userPrefTag WHERE id_user = " + Sessions.getUserId();
        List<String> result = new ArrayList<>();
        ConnectDB db = new ConnectDB();
        Connection con = db.getConnetion();
        if(con == null) return result;
        try(PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){
            while(rs.next()) {
                System.out.println(rs.getString("id_tag"));
                result.add(rs.getString("id_tag"));
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }
        return result;
    }

    public static List<String> getAllTags(){
        List<String> tags = new ArrayList<>();
        ConnectDB db = new ConnectDB();
        Connection con = db.getConnetion();
        if(con == null){
            System.out.println("failed to connect to DB");
            return tags;
        }

        String query = "SELECT nama_tag FROM tag ORDER BY id_tag ASC";
        try (PreparedStatement prepare = con.prepareStatement(query);
            ResultSet rs = prepare.executeQuery()){
                while(rs.next()) {
                    tags.add(rs.getString("nama_tag"));
                } } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error getting tags");
        }finally{
                db.closeConnection();

    }
        return tags;
} }

