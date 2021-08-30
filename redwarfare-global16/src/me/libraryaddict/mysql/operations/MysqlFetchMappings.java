package me.libraryaddict.mysql.operations;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class MysqlFetchMappings extends DatabaseOperation {
    private ConcurrentHashMap<Integer, String> _keys = new ConcurrentHashMap<Integer, String>();

    public MysqlFetchMappings() {
        Connection con = null;

        try {
            con = getMysql();

            //Statement stmt = con.createStatement();
            //in some new paper version a billion exceptions are thrown for scrolling backwards
            // on a resultset of type forward only.
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM mappings");

            rs.beforeFirst();

            while (rs.next()) {
                _keys.put(rs.getInt("value"), rs.getString("name"));
            }

            setSuccess();
        } catch (Exception ex) {
            UtilError.handle(ex);
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                UtilError.handle(e);
            }
        }
    }

    public ConcurrentHashMap<Integer, String> getKeys() {
        return _keys;
    }
}
