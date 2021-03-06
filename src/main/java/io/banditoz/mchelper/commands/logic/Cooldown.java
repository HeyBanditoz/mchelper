package io.banditoz.mchelper.commands.logic;

import io.banditoz.mchelper.utils.DateUtils;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {
    private final CooldownType type;
    private final int time;
    private final ChronoUnit timeUnit;
    private final Map<ISnowflake, Instant> map = new ConcurrentHashMap<>();

    public Cooldown(int time, ChronoUnit timeUnit, CooldownType type) {
        this.type = type;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    /**
     * Checks if any given entity is on cooldown.
     *
     * @param entity The entity to check.
     * @return true if they are allowed to run the command, false if they are still on cooldown.
     */
    public boolean handle(ISnowflake entity) {
        Instant cooldown = map.get(entity);
        if (cooldown == null) {
            Instant instant = Instant.now().plus(time, timeUnit);
            map.put(entity, instant);
            return true;
        }
        else if (Instant.now().isAfter(cooldown)) {
            map.replace(entity, Instant.now().plus(time, timeUnit));
            return true;
        }
        else {
            return false;
        }
    }

    public void remove(ISnowflake entity) {
        map.remove(entity);
    }

    public boolean isOnCooldown(ISnowflake entity) {
        Instant cooldown = map.get(entity);
        if (cooldown == null) {
            return false;
        }
        else return !Instant.now().isAfter(cooldown);
    }

    public String getRemainingTime(ISnowflake entity) {
        Instant cooldown = map.get(entity);
        if (cooldown == null) {
            return null;
        }
        else if (Instant.now().isBefore(cooldown)) {
            return DateUtils.humanReadableDuration(Duration.between(Instant.now(), cooldown));
        }
        else {
            return null;
        }
    }

    public CooldownType getType() {
        return type;
    }
}
