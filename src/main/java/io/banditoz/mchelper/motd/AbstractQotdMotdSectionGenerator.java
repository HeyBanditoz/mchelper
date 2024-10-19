package io.banditoz.mchelper.motd;

import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.utils.database.NamedQuote;
import io.banditoz.mchelper.utils.database.dao.QuotesDao;
import io.banditoz.mchelper.utils.database.dao.QuotesDaoImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.sql.SQLException;

public abstract class AbstractQotdMotdSectionGenerator extends MotdSectionGenerator {
    protected final boolean onlyExcluded;
    protected final Color embedColor;

    public AbstractQotdMotdSectionGenerator(MCHelper mcHelper, boolean onlyExcluded, Color embedColor) {
        super(mcHelper);
        this.onlyExcluded = onlyExcluded;
        this.embedColor = embedColor;
    }

    @Override
    public MessageEmbed generate(TextChannel tc) {
        QuotesDao dao = new QuotesDaoImpl(mcHelper.getDatabase());
        try {
            NamedQuote nq = dao.getRandomQotd(tc.getGuild(), onlyExcluded);
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
