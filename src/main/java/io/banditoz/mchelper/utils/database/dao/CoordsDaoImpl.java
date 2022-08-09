package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.CoordinatePoint;
import io.banditoz.mchelper.utils.database.Database;
import io.jenetics.facilejdbc.Param;
import io.jenetics.facilejdbc.Query;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CoordsDaoImpl extends Dao implements CoordsDao {
    public CoordsDaoImpl(Database database) {
        super(database);
    }

    @Override
    public void savePoint(CoordinatePoint point) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("INSERT INTO coordinates VALUES (:g, :a, :n, :x, :z, (SELECT NOW()))")
                    .on(
                            Param.value("g", point.getGuildId()),
                            Param.value("a", point.getAuthorId()),
                            Param.value("n", point.getName()),
                            Param.value("x", point.getX()),
                            Param.value("z", point.getZ())
                    ).executeUpdate(c);
        }
    }

    @Override
    public CoordinatePoint getPointByName(String name, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM coordinates WHERE name=:n AND guild_id=:g")
                    .on(
                            Param.value("n", name),
                            Param.value("g", g.getIdLong())
                    )
                    .as(this::parseOne, c);
        }
    }

    @Override
    public void deletePointByName(String name, Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            Query.of("DELETE FROM coordinates WHERE name=:n AND guild_id=:g")
                    .on(
                            Param.value("n", name),
                            Param.value("g", g.getIdLong())
                    ).executeUpdate(c);
        }
    }

    @Override
    public List<CoordinatePoint> getAllPointsForGuild(Guild g) throws SQLException {
        try (Connection c = DATABASE.getConnection()) {
            return Query.of("SELECT * FROM coordinates WHERE guild_id=:g")
                    .on(Param.value("g", g.getIdLong()))
                    .as((rs, conn) -> parseMany(rs, conn, this::parseOne), c);
        }
    }

    private CoordinatePoint parseOne(ResultSet rs, Connection c) throws SQLException {
        if (!rs.next()) {
            return null;
        }
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
