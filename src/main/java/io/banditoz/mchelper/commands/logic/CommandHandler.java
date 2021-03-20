package io.banditoz.mchelper.commands.logic;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.*;
import io.banditoz.mchelper.stats.Stat;
import io.banditoz.mchelper.stats.Status;
import io.banditoz.mchelper.utils.Settings;
import io.banditoz.mchelper.utils.database.dao.GuildConfigDaoImpl;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

public class CommandHandler extends ListenerAdapter {
    /** The command map. String is the command name (what the user types) and Command is the command. */
    private final Map<String, Command> commands = new HashMap<>();
    private final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private final MCHelper MCHELPER;
    private int commandsRun;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong())
            return; // don't execute own commands
        getCommandByEvent(event).ifPresent(c -> {
            if (c.canExecute(event, MCHELPER)) {
                MCHELPER.getThreadPoolExecutor().execute(() -> {
                    Stat s = c.execute(event, MCHELPER);
                    if (s.getStatus() == Status.SUCCESS) {
                        commandsRun++;
                    }
                    LOGGER.info(s.getLogMessage());
                    MCHELPER.getStatsRecorder().record(s);
                });
            }
        });
    }

    protected Optional<Command> getCommandByEvent(MessageReceivedEvent e) {
        String[] args = CommandUtils.commandArgs(e.getMessage().getContentDisplay());
        if (args.length == 0) {
            return Optional.empty();
        }
        char prefix = '!';
        if (e.isFromGuild()) {
            prefix = new GuildConfigDaoImpl(MCHELPER.getDatabase()).getConfig(e.getGuild()).getPrefix();
        }
        if (args[0].charAt(0) != prefix) {
            return Optional.empty();
        }
        return Optional.ofNullable(commands.get(args[0].substring(1).toLowerCase())); // remove the prefix from the command arg
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public boolean removeCommandByName(String name) {
        return commands.remove(name) != null;
    }

    public CommandHandler(MCHelper MCHelper) {
        this.MCHELPER = MCHelper;
        Settings settings = MCHelper.getSettings();
        LOGGER.info("Registering commands and listeners...");
        add(new BashCommand());
        add(new InfoCommand());
        add(new MathCommand());
        add(new EangleCommand());
        add(new NetherCommand());
        add(new OverworldCommand());
        add(new UnitsCommand());
        add(new TeXCommand());
        add(new PickCommand());
        add(new ToMorseCommand());
        add(new FromMorseCommand());
        add(new EvalCommand());
        add(new DiceRollerCommand());
        add(new CoinFlipCommand());
        add(new VersionCommand());
        add(new PingCommand());
        add(new HeapDumpCommand());
        add(new UploadLogsCommand());
        add(new FloodCommand());
        add(new SnowflakeCommand());
        add(new InviteBotCommand());
        add(new RockPaperScissorsCommand());
        add(new ServerStatusCommand());
        add(new UrbanDictionaryCommand());
        add(new PlotCommand());
        add(new TeamsCommand());
        add(new UserInfoCommand());
        add(new JoinOrderCommand());
        add(new EightBallCommand());
        add(new RussianRouletteCommand());
        add(new RemoveCommandCommand());
        add(new CooldownsCommand());

        if (settings.getDatabaseHostAndPort() != null && !settings.getDatabaseHostAndPort().equals("Host and port of the database.")) {
            add(new CoordCommand());
            add(new QuoteCommand());
            add(new AddquoteCommand());
            add(new SqlCommand());
            add(new RemindmeCommand());
            add(new DeleteReminderCommand());
            add(new DefaultChannelCommand());
            add(new PrefixCommand());
            add(new StatisticsCommand());
            add(new ManageRolesCommand());
            add(new DeleteQuoteCommand());
            add(new TransferCommand());
            add(new BalanceCommand());
            add(new BaltopCommand());
            add(new TransactionsCommand());
            add(new DoubleOrNothingCommand());
            add(new BalanceGraphCommand());
            add(new BlackJackCommand());
            try {
                add(new WorkCommand(MCHELPER.getObjectMapper()));
            } catch (Exception ex) {
                LOGGER.error("Error while initializing the work responses.", ex);
            }
        }

        if (settings.getOwlBotToken() == null || settings.getOwlBotToken().equals("OwlBot API key here.")) {
            LOGGER.info("No OwlBot API key defined! Not enabling the dictionary define command...");
        }
        else {
            add(new DictionaryCommand());
        }

        if (settings.getFinnhubKey() == null || settings.getFinnhubKey().equals("Alpha Vantage API key here")) {
            LOGGER.info("Finnhub API key not defined! Not enabling financial commands.");
        }
        else {
            add(new StockCommand());
        }

        if (settings.getRiotApiKey() == null || settings.getRiotApiKey().equals("Riot Api Key here")) {
            LOGGER.info("Riot API key not defined! Not enabling Orianna.");
        }
        else {
            Orianna.setRiotAPIKey(settings.getRiotApiKey());
            Orianna.setDefaultRegion(Region.NORTH_AMERICA);
            add(new LoadoutCommand());
        }

        if (settings.getTarkovMarketApiKey() == null || settings.getTarkovMarketApiKey().equals("https://tarkov-market.com API key here.")) {
            LOGGER.info("No tarkov-market API key defined! Not enabling the Tarkov market command...");
        }
        else {
            add(new TarkovCommand());
        }
        add(new HelpCommand(commands.values())); // this must be registered last
        LOGGER.info(commands.size() + " commands registered.");
    }

    private void add(Command c) {
        commands.put(c.commandName(), c);
    }

    public int getCommandsRun() {
        return commandsRun;
    }
}
