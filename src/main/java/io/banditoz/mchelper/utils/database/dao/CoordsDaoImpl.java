package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.CoordinatePoint;
import io.banditoz.mchelper.utils.database.Database;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoordsDaoImpl extends Dao implements CoordsDao {
    @Override
    public String getSqlTableGenerator() {
        return "CREATE TABLE IF NOT EXISTS `coordinates`( `guild_id` bigint(18) NOT NULL, `author_id` bigint(18) NOT NULL, `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL, `x` bigint(20) NOT NULL, `z` bigint(20) NOT NULL, `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(), UNIQUE KEY `guild_name` (`guild_id`,`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    @Override
    public void savePoint(CoordinatePoint point) throws SQLException {
        PreparedStatement ps = Database.getConnection().prepareStatement("INSERT INTO coordinates VALUES (?, ?, ?, ?, ?, (SELECT NOW()))");
        ps.setLong(1, point.getGuildId());
        ps.setLong(2, point.getAuthorId());
        ps.setString(3, point.getName());
        ps.setLong(4, point.getX());
        ps.setLong(5, point.getZ());
        ps.execute();
        ps.close();
    }

    @Override
    public CoordinatePoint getPointByName(String name, Guild g) throws SQLException {
        PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM coordinates WHERE name=(?) AND guild_id=(?)");
        ps.setString(1, name);
        ps.setLong(2, g.getIdLong());
        CoordinatePoint point = buildPointFromResultSet(ps.executeQuery());
        ps.close();
        return point;
    }

    @Override
    public void deletePointByName(String name, Guild g) throws SQLException {
        PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM coordinates WHERE name=(?) AND guild_id=(?)");
        ps.setString(1, name);
        ps.setLong(2, g.getIdLong());
        ps.execute();
        ps.close();
    }

    @Override
    public List<CoordinatePoint> getAllPointsForGuild(Guild g) throws SQLException {
        ArrayList<CoordinatePoint> points = new ArrayList<>();
        PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM coordinates WHERE guild_id=(?)");
        ps.setLong(1, g.getIdLong());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            points.add(buildPointFromResultSet(rs));
        }
        ps.close();
        return points;
    }

    private CoordinatePoint buildPointFromResultSet(ResultSet rs) throws SQLException {
        CoordinatePoint point = new CoordinatePoint();
        point.setGuildId(rs.getLong("guild_id"));
        point.setAuthorId(rs.getLong("author_id"));
        point.setName(rs.getString("name"));
        point.setX(rs.getLong("x"));
        point.setZ(rs.getLong("z"));
        point.setLastModified(rs.getTimestamp("last_modified"));
        return point;
    }
}
