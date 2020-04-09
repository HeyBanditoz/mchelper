package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.CoordinatePoint;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.List;

public interface CoordsDao {
    void savePoint(CoordinatePoint point) throws SQLException;
    CoordinatePoint getPointByName(String name, Guild g) throws SQLException;
    void deletePointByName(String name, Guild g) throws SQLException;
    List<CoordinatePoint> getAllPointsForGuild(Guild g) throws SQLException;
}
