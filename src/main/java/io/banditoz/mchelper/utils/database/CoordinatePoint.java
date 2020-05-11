package io.banditoz.mchelper.utils.database;

import java.sql.Timestamp;

public class CoordinatePoint {
    private long x;
    private long z;
    private String name;
    private long authorId;
    private long guildId;
    private Timestamp lastModified;

    public CoordinatePoint(String x, String z, String name, long authorId, long guildId) {
        this.x = Long.parseLong(x);
        this.z = Long.parseLong(z);
        this.name = name;
        this.authorId = authorId;
        this.guildId = guildId;
    }

    public CoordinatePoint() {
    }

    public CoordinatePoint(long x, long z) {
        this.x = x;
        this.z = z;
    }

    public CoordinatePoint(String x, String z) {
        this.x = Long.parseLong(x);
        this.z = Long.parseLong(z);
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getZ() {
        return z;
    }

    public void setZ(long z) {
        this.z = z;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return x + ", " + z;
    }

    public double getAngleBetweenTwoPoints(CoordinatePoint point) {
        double angle = Math.toDegrees(Math.atan2((point.z - this.z), (point.x - this.x)));
        if (angle < -90) {
            return angle + 270;
        }
        else {
            return angle - 90;
        }
    }

    public double getDistance(CoordinatePoint point) {
        double a = (this.x - point.x);
        double b = (this.z - point.z);

        return Math.sqrt((a * a) + (b * b));
    }

    public CoordinatePoint getNetherCoordinates() {
        return new CoordinatePoint(this.x / 8, this.z / 8);
    }

    public CoordinatePoint getOverworldCoordinates() {
        return new CoordinatePoint(this.x * 8, this.z * 8);
    }

    public String toIntegerString() {
        return x + ", " + z;
    }
}
