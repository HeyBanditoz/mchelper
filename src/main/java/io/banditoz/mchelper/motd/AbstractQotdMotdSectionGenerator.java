package io.banditoz.mchelper.motd;

import java.awt.Color;
import java.sql.SQLException;

import io.avaje.inject.BeanScope;
import io.banditoz.mchelper.database.NamedQuote;
import io.banditoz.mchelper.database.dao.QuotesDao;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public abstract class AbstractQotdMotdSectionGenerator extends MotdSectionGenerator {
    protected final boolean onlyExcluded;
    protected final Color embedColor;

    public AbstractQotdMotdSectionGenerator(BeanScope beanScope, boolean onlyExcluded, Color embedColor) {
        super(beanScope);
        this.onlyExcluded = onlyExcluded;
        this.embedColor = embedColor;
    }

    @Override
    public MessageEmbed generate(TextChannel tc) {
        try {
            NamedQuote nq = beanScope.get(QuotesDao.class).getRandomQotd(tc.getGuild(), onlyExcluded);
            if (nq == null) {
                return null; // guild has no quotes, rip
            }
            return new EmbedBuilder().setTitle("Quote of the Day").setColor(embedColor).setDescription(nq.format(false)).build();
        } catch (SQLException ex) {
            log.warn("Error fetching QOTD for guild {}.", tc.getGuild());
            return null;
        }
    }
}
