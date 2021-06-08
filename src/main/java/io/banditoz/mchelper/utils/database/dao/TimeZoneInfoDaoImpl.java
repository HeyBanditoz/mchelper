package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.TimeZone;

public class TimeZoneInfoDaoImpl extends Dao implements TimeZoneInfoDao {
    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `timezones`( `tz_id` varchar(50) NOT NULL, PRIMARY KEY (`tz_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1; CREATE TABLE IF NOT EXISTS `user_timezone`( `id` varchar(18) NOT NULL, `tz` varchar(50) DEFAULT NULL, PRIMARY KEY (`id`), KEY `fk_tz_id` (`tz`), CONSTRAINT `fk_tz_id` FOREIGN KEY (`tz`) REFERENCES `timezones` (`tz_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1";
    }

    public TimeZoneInfoDaoImpl(Database database) {
        super(database);
    }

    public void insertOrReplaceTimeZone(long id, TimeZone tz) throws SQLException {
        insertTimeZone(tz);
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("REPLACE INTO user_timezone VALUES (?, ?)");
            ps.setLong(1, id);
            ps.setString(2, tz.getID());
            ps.execute();
            ps.close();
        }
    }

    public Optional<TimeZone> getTimeZoneForId(long id) throws SQLException {
        Optional<TimeZone> returnedTz;
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT tz FROM user_timezone WHERE id = ?");
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    returnedTz = Optional.empty();
                }
                else {
                    returnedTz = Optional.of(TimeZone.getTimeZone(rs.getString(1)));
                }
            }
            ps.close();
        }
        return returnedTz;
    }

    private void insertTimeZone(TimeZone tz) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO timezones VALUES (?);");
            ps.setString(1, tz.getID());
            ps.execute();
            ps.close();
        }
    }

}
