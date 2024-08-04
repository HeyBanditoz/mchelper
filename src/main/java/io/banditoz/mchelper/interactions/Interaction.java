package io.banditoz.mchelper.interactions;

import net.dv8tion.jda.api.entities.User;

public interface Interaction {
    User getUser();
    void removeListener();
}
