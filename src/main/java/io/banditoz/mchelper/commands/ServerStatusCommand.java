package io.banditoz.mchelper.commands;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.banditoz.mchelper.commands.logic.BoundCommand;
import io.banditoz.mchelper.commands.logic.ICommandEvent;
import io.banditoz.mchelper.commands.logic.params.Handler;
import io.banditoz.mchelper.commands.logic.params.Params;
import io.banditoz.mchelper.serverstatus.MinecraftServerStatus;
import io.banditoz.mchelper.serverstatus.StatusResponse;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Help;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

@Singleton
public class ServerStatusCommand extends BoundCommand {
    private final ObjectMapper objectMapper;

    @Inject
    public ServerStatusCommand(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String commandName() {
        return "status";
    }

    @Override
    protected Handler declare() {
        return Params.of().rest("host", "Hostname and port.", this::check);
    }

    @Override
    protected String description() {
        return "Fetches information about a Minecraft server.";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<address[:port]>")
                .withDescription("Fetches information about a Minecraft server.");
    }

    public Status check(ICommandEvent ce, String input) throws Exception {
        String[] address = input.split(":"); // 127.0.0.1:25565 will be split correctly into host then port
        String host = address[0];
        int port = 25565; // minecraft default
        if (address.length > 1) {
            port = Integer.parseInt(address[1]);
        }
        InetSocketAddress addr = new InetSocketAddress(host, port);
        try {
            StatusResponse response = MinecraftServerStatus.fetchData(addr, 5000, objectMapper);
            String description = MarkdownSanitizer.escape(response.getDescriptionAsString());
            String randomUUID = UUID.randomUUID().toString().replace("-", "");
            String players = response.players().sample().stream()
                    .sorted()
                    .map(player -> MarkdownSanitizer.escape(player.name()))
                    .collect(Collectors.joining(", "));
            MessageEmbed me = new EmbedBuilder()
                    .setTitle(addr.toString())
                    .setDescription(description.isEmpty() ? "<no description>" : description)
                    .addField("Players", String.format("%d/%d", response.players().online(), response.players().max()), true)
                    .addField("Version", String.format("%s (%s)", response.version().name(), response.version().protocol()), true)
                    .addField("Player Sample", players.isEmpty() ? "<no player sample>" : players, false)
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
