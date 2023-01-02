package io.banditoz.mchelper.runnables;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.dao.UserCacheDao;
import io.banditoz.mchelper.utils.database.dao.UserCacheDaoImpl;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserMaintenanceRunnable implements Runnable {
    private final MCHelper MCHELPER;
    private final UserCacheDao DAO;
    private final Logger LOGGER = LoggerFactory.getLogger(UserMaintenanceRunnable.class);

    public UserMaintenanceRunnable(MCHelper mcHelper) {
        this.MCHELPER = mcHelper;
        DAO = new UserCacheDaoImpl(mcHelper.getDatabase());
    }

    @Override
    public void run() {
        try {
            int dbCount = DAO.getCachedUserCount();
            List<User> users = MCHELPER.getJDA().getUsers();
            DAO.replaceAll(users);
            DAO.deleteNonexistentUsers(users);
            int diff = users.size() - dbCount;
            LOGGER.info(String.format("Successfully performed maintenance on the user cache. Cache: %d, Change: %d.", users.size(), diff));
        } catch (Exception ex) {
            LOGGER.error("Error while performing user cache maintenance!", ex);
        }
    }
}
