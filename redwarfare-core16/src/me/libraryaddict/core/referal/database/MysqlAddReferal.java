package me.libraryaddict.core.referal.database;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class MysqlAddReferal extends DatabaseOperation {
    public MysqlAddReferal(UUID referer, UUID refered, String referedName) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO referals (referer, refered, refered_name) VALUES (?,?,?)");

            stmt.setString(1, referer.toString());
            stmt.setString(2, refered.toString());
            stmt.setString(3, referedName);

            stmt.execute();

            setSuccess();
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
