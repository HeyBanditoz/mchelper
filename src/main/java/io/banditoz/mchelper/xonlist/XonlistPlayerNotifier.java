package io.banditoz.mchelper.xonlist;

import io.banditoz.mchelper.http.XonlistClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.sanitize;

@Singleton
public class XonlistPlayerNotifier {
    private final XonlistClient xonlistClient;

    private static final Color XONOTIC_COLOR = new Color(20, 254, 255);
    private static final Comparator<XonoticServer> REVERSE_PLAYER_COUNT_COMPARATOR = Comparator.comparing(XonoticServer::numPlayers).reversed();

    @Inject
    public XonlistPlayerNotifier(XonlistClient xonlistClient) {
        this.xonlistClient = xonlistClient;
    }

    public void checkAndNotify(TextChannel notifChannel) {
        XonlistResponse xonlist = xonlistClient.getAllXonoticServers();
        List<XonoticServer> filteredServers = xonlist.servers()
                .values()
                .stream()
                // replace with ping check instead of wacky geo, and don't hardcode this
                .filter(xs -> "US".equals(xs.geo()) || "CA".equals(xs.geo()))
                .filter(xs -> xs.numPlayers() >= 6)
                .sorted(REVERSE_PLAYER_COUNT_COMPARATOR)
                .toList();
        if (filteredServers.isEmpty()) {
            return;
        }
        StringBuilder serverMd = new StringBuilder();
        for (XonoticServer srv : filteredServers) {
            serverMd.append("* ")
                    .append(srv.numPlayers())
                    .append("/")
                    .append(srv.maxPlayers())
                    .append(" \\- ")
                    .append(sanitize(srv.realName()))
                    .append("\n  * `")
                    .append(srv.address())
                    .append("` (in ")
                    .append(sanitize(srv.geo()))
                    .append(")\n");
        }

        MessageEmbed me = new EmbedBuilder()
                .setTitle("%d active Xonotic server%s".formatted(filteredServers.size(), filteredServers.size() > 1 ? "s!" : "!"))
                .setDescription(serverMd.toString())
                .setColor(XONOTIC_COLOR)
                .setTimestamp(Instant.ofEpochSecond(xonlist.info().lastUpdate_epoch()))
                .build();
        notifChannel.sendMessageEmbeds(me).queue();
    }
}
