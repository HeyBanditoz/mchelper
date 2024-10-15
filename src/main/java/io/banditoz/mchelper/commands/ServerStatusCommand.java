package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.serverstatus.MinecraftServerStatus;
import io.banditoz.mchelper.serverstatus.Player;
import io.banditoz.mchelper.serverstatus.StatusResponse;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public class ServerStatusCommand extends Command {
    @Override
    public String commandName() {
        return "status";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<address[:port]>")
                .withDescription("Fetches information about a Minecraft server.");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        String[] address = ce.getCommandArgs()[1].split(":"); // 127.0.0.1:25565 will be split correctly into host then port
        String host = address[0];
        int port = 25565; // minecraft default
        if (address.length > 1) {
            port = Integer.parseInt(address[1]);
        }
        InetSocketAddress addr = new InetSocketAddress(host, port);
        MinecraftServerStatus status = new MinecraftServerStatus(addr, 5000, ce.getMCHelper().getObjectMapper());
        try {
            StatusResponse response = status.fetchData();
            String description = MarkdownSanitizer.escape(response.getDescriptionAsString());
            String randomUUID = UUID.randomUUID().toString().replace("-", "");
            List<Player> players = new ArrayList<>(response.getPlayers().getSample());
            players.sort(Player::compareTo);
            StringJoiner sj = new StringJoiner(", ");
            players.forEach(p -> sj.add(MarkdownSanitizer.escape(p.getName())));
            MessageEmbed me = new EmbedBuilder()
                    .setTitle(addr.toString())
                    .setDescription(description.isEmpty() ? "<no description>" : description)
                    .addField("Players", String.format("%d/%d", response.getPlayers().getOnline(), response.getPlayers().getMax()), true)
                    .addField("Version", String.format("%s (%s)", response.getVersion().getName(), response.getVersion().getProtocol()), true)
                    .addField("Player Sample", players.isEmpty() ? "<no player sample>" : sj.toString(), false)
                    .setThumbnail(String.format("attachment://%s.png", randomUUID))
                    .setColor(Color.GREEN)
                    .build();
            ce.sendEmbedThumbnailReply(me, response.getFaviconAsImage(), randomUUID);
            return Status.SUCCESS;
        } catch (IOException ex) {
            ce.sendEmbedReply(new EmbedBuilder()
                    .setTitle("Could not fetch server status for " + addr.toString())
                    .setColor(Color.RED)
                    .setDescription(ex.toString()).build());
            LOGGER.warn("Error fetching server status for " + addr, ex);
            return Status.FAIL;
        }
    }
}
