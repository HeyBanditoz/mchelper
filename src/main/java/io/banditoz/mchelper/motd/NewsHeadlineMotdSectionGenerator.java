package io.banditoz.mchelper.motd;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.banditoz.mchelper.utils.StringUtils.truncate;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.config.Config;
import io.banditoz.mchelper.config.ConfigurationProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class NewsHeadlineMotdSectionGenerator extends MotdSectionGenerator {
    public NewsHeadlineMotdSectionGenerator(MCHelper mcHelper) {
        super(mcHelper);
    }

    @Override
    public MessageEmbed generate(TextChannel tc) {
        ConfigurationProvider configurationProvider = mcHelper.getConfigurationProvider();
        String[] urls = configurationProvider.getValue(Config.RSS_URLS, tc.getGuild()).split(" ");

        Map<Channel, List<Item>> stories = mcHelper.getRssScraper().getRssForAll(urls);
        EmbedBuilder eb = new EmbedBuilder().setTitle("News Stories Today").setColor(new Color(23, 183, 204));
        stories.keySet()
                .stream()
                .map(ChannelWrapper::new)
                .distinct()
                .map(ChannelWrapper::channel)
                .sorted(Comparator.comparing(Channel::getTitle))
                .forEach(channel -> {
            Item item = stories.get(channel).get(0);
            String title = truncate(channel.getTitle(), 253, false);
//            String mdUrl = "[To Article](%s)".formatted(item.getLink().orElseGet(() -> item.getGuid().orElse("<no article>")));
            String mdUrl = "[To Article]";
            if (mdUrl.contains("<no article>")) {
                mdUrl = "<no link>"; // a bit silly, but a guard just in case
            }
            String body = truncate(item.getTitle().orElse("<no title>") + ' ' + mdUrl, 1020 - mdUrl.length(), false);
            eb.addField(title, body, false);
        });
        return eb.build();
    }

    private record ChannelWrapper(Channel channel) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChannelWrapper wrapper = (ChannelWrapper) o;
            return Objects.equals(channel.getTitle(), wrapper.channel.getTitle());
        }
    }
}
