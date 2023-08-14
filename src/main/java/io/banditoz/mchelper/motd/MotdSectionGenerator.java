package io.banditoz.mchelper.motd;

import io.banditoz.mchelper.MCHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MotdSectionGenerator {
    protected final MCHelper mcHelper;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public MotdSectionGenerator(MCHelper mcHelper) {
        this.mcHelper = mcHelper;
    }

    public abstract @Nullable MessageEmbed generate(TextChannel tc);
}
