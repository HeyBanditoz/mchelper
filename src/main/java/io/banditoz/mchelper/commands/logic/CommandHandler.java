package io.banditoz.mchelper.commands.logic;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.SettingsManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandHandler extends ListenerAdapter {
    private final List<Command> commands;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private static final Settings SETTINGS = SettingsManager.getInstance().getSettings();

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        getCommandByEvent(event).ifPresent(c -> c.tryToExecute(event));
    }

    protected Optional<Command> getCommandByEvent(MessageReceivedEvent e) {
        return commands.stream()
                .filter(c -> c.containsCommand(e))
                .findAny();
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public CommandHandler() {
        LOGGER.info("Registering commands and listeners...");
        commands = new ArrayList<>();
        commands.add(new BashCommand());
        commands.add(new InfoCommand());
        commands.add(new MathCommand());
        commands.add(new EangleCommand());
        commands.add(new NetherCommand());
        commands.add(new OverworldCommand());
        commands.add(new UnitsCommand());
        commands.add(new CoordCommand());
        commands.add(new TeXCommand());
        commands.add(new PickCommand());
        commands.add(new ToMorseCommand());
        commands.add(new FromMorseCommand());
        commands.add(new EvalCommand());
        commands.add(new DiceRollerCommand());
        commands.add(new CoinFlipCommand());
        commands.add(new VersionCommand());
        commands.add(new PingCommand());
        commands.add(new DefaultChannelCommand());
        commands.add(new PrefixCommand());
        commands.add(new HeapDumpCommand());
        commands.add(new UploadLogsCommand());
        commands.add(new FloodCommand());
        commands.add(new QuoteCommand());
        commands.add(new AddquoteCommand());
        commands.add(new SqlCommand());
        commands.add(new RemindmeCommand());
        commands.add(new DeleteReminderCommand());
        commands.add(new SnowflakeCommand());
        commands.add(new InviteBotCommand());
        commands.add(new RockPaperScissorsCommand());

        if (SETTINGS.getOwlBotToken() == null || SETTINGS.getOwlBotToken().equals("OwlBot API key here.")) {
            LOGGER.info("No OwlBot API key defined! Not enabling the dictionary define command...");
        }
        else {
            commands.add(new DictionaryCommand());
        }

        if (SETTINGS.getEsUrl() == null || SETTINGS.getGrafanaToken() == null || SETTINGS.getGrafanaUrl() == null) {
            LOGGER.info("No weather station configs defined! Not enabling the weather station command...");
        }
        else {
            commands.add(new WeatherStationCommand());
        }

        if (SETTINGS.getAlphaVantageKey() == null || SETTINGS.getAlphaVantageKey().equals("Alpha Vantage API key here")) {
            LOGGER.info("Alpha Vantage API key not defined! Not enabling financial commands.");
        }
        else {
            commands.add(new CurrencyConversionCommand());
            commands.add(new StockCommand());
        }

        if (SETTINGS.getRiotApiKey() == null || SETTINGS.getRiotApiKey().equals("Riot Api Key here")) {
            LOGGER.info("Riot API key not defined! Not enabling Orianna.");
        }
        else {
            Orianna.setRiotAPIKey(SETTINGS.getRiotApiKey());
            Orianna.setDefaultRegion(Region.NORTH_AMERICA);
            Thread thread = new Thread(LoadoutCommand::createData);
            thread.setName("Orianna");
            thread.start();
            commands.add(new LoadoutCommand());
        }
        commands.add(new HelpCommand(commands)); // this must be registered last
        LOGGER.info(commands.size() + " commands registered.");
    }
}
