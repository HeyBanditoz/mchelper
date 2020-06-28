package io.banditoz.mchelper.utils.database.dao;

import io.banditoz.mchelper.utils.database.CoordinatePoint;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.SQLException;
import java.util.List;

public interface CoordsDao {
    /**
     * Saves a coordinate point to the database
     *
     * @param point The {@link CoordinatePoint} to save.
     * @throws SQLException If there was an error saving the point.
     */
    void savePoint(CoordinatePoint point) throws SQLException;
    /**
     * Gets a coordinate point by its name and guild if it exists. It will return null or throw a {@link SQLException}
     * if it doesn't exist.
     *
     * @param name The name of the point to fetch.
     * @param g    The guild to fetch.
     * @return The {@link CoordinatePoint} to fetch.
     * @throws SQLException If the point doesn't exist.
     */
    CoordinatePoint getPointByName(String name, Guild g) throws SQLException;
    /**
     * Deletes a point by its name and guild if it exists.
     *
     * @param name The name of the point to delete.
     * @param g    The guild to fetch.
     * @throws SQLException If there was an error deleting the point.
     */
    void deletePointByName(String name, Guild g) throws SQLException;
    /**
     * Gets all the coordinate points that exist in a guild.
     *
     * @param g The guild to get all the points from.
     * @return A {@link List} containing all the points.
     * @throws SQLException If there aren't any coordinate points.
     */
    List<CoordinatePoint> getAllPointsForGuild(Guild g) throws SQLException;
}
