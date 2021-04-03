package me.libraryaddict.core.referal.database;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class MysqlDeleteReferal extends DatabaseOperation {
    public MysqlDeleteReferal(UUID refered) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM referals WHERE refered = ?");
            stmt.setString(1, refered.toString());

            stmt.execute();
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
