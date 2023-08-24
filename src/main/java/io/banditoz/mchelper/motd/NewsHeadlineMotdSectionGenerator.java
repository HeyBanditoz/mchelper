package io.banditoz.mchelper.motd;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static io.banditoz.mchelper.utils.StringUtils.truncate;

public class NewsHeadlineMotdSectionGenerator extends MotdSectionGenerator {
    public NewsHeadlineMotdSectionGenerator(MCHelper mcHelper) {
        super(mcHelper);
    }

    @Override
    public MessageEmbed generate(TextChannel tc) {
        ConfigurationProvider configurationProvider = new ConfigurationProvider(mcHelper);
        String[] urls = configurationProvider.getValue(Config.RSS_URLS, tc.getGuild().getIdLong()).split(" ");

        Map<Channel, List<Item>> stories = mcHelper.getRssScraper().getRssForAll(urls);
        EmbedBuilder eb = new EmbedBuilder().setTitle("News Stories Today").setColor(new Color(23, 183, 204));
        stories.keySet().stream().sorted(Comparator.comparing(Channel::getTitle)).forEach(channel -> {
            Item item = stories.get(channel).get(0);
            String title = truncate(channel.getTitle(), 253, false);
            String mdUrl = "[To Article](%s)".formatted(item.getLink().orElseGet(() -> item.getGuid().orElse("<no article>")));
            if (mdUrl.contains("<no article>")) {
                mdUrl = "<no link>"; // a bit silly, but a guard just in case
            }
            String body = truncate(item.getTitle().orElse("<no title>") + ' ' + mdUrl, 1020 - mdUrl.length(), false);
            eb.addField(title, body, false);
        });
        return eb.build();
    }
}