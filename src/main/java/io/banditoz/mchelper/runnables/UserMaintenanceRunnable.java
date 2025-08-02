package io.banditoz.mchelper.runnables;

import java.util.List;

import io.banditoz.mchelper.database.dao.UserCacheDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserMaintenanceRunnable implements Runnable {
    private final JDA jda;
    private final UserCacheDao dao;
    private final Logger LOGGER = LoggerFactory.getLogger(UserMaintenanceRunnable.class);

    @Inject
    public UserMaintenanceRunnable(JDA jda,
                                   UserCacheDao dao) {
        this.jda = jda;
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            int dbCount = dao.getCachedUserCount();
            List<User> users = jda.getUsers();
            dao.replaceAll(users);
            dao.deleteNonexistentUsers(users);
            int diff = users.size() - dbCount;
            LOGGER.info(String.format("Successfully performed maintenance on the user cache. Cache: %d, Change: %d.", users.size(), diff));
        } catch (Exception ex) {
            LOGGER.error("Error while performing user cache maintenance!", ex);
        }
    }
}
