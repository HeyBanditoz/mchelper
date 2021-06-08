package io.banditoz.mchelper.utils.database.dao;

import java.sql.SQLException;
import java.util.Optional;
import java.util.TimeZone;

public interface TimeZoneInfoDao {
    void insertOrReplaceTimeZone(long id, TimeZone tz) throws SQLException;
    Optional<TimeZone> getTimeZoneForId(long id) throws SQLException;
}
