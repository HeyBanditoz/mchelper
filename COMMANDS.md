# Commands
There are a total of 65 commands and 6 regex listeners.
### AddquoteCommand
`addquote` - "<quote>" <author> - Adds a quote to the database.
### BalanceCommand
`bal` - <no parameters>  - Checks your balance.
### BalanceGraphCommand
`balgraph` - [-d] - Graphs your transaction history, transaction by transaction. Use -d to use dates instead
### BaltopCommand
`baltop` - <no parameters>  - Gets the money leaderboard for this guild.
### BashCommand
`bash` - <bash> - Executes bash if it is installed on system. (ELEVATED)
### BlackJackCommand
`blackjack` - <ante (with range 5 <= x <= 200000)> - Play double or nothing to win some money!
### CoinFlipCommand
`flip` - <no parameters>  - Flips a coin.
### CooldownsCommand
`cooldowns` - [user as mention] - Returns the cooldowns for you or another user.
### CoordCommand
`coords` - <save|add,show|list,delete|remove,help> - Saves coordinates to the database. See !coords help
### DeleteQuoteCommand
`delquote` - <quote ID> - Deletes a quote from the database. You must have MANAGE_SERVER permissions on the guild.
### DeleteReminderCommand
`delremind` - <id> - Deletes a reminder.
### DiceRollerCommand
`roll` - [dice notation] - Roll some dice following standard dice notation.
### DictionaryCommand
`define` - <word> - Finds the definition of a word using Owlbot's API.
### DoubleOrNothingCommand
`don` - <ante (with range 5 <= x <= 200000)> - Play double or nothing to win some money!
### DuelCommand
`duel` - <ante> - Duel with your channel for money!
### EangleCommand
`eangle` - <x1> <z1> <x2> <z2> - Returns the elytra flight angle and distance between two Minecraftian coordinate points.
### EightBallCommand
`8` - <no parameters>  - Seek wisdom of the eight ball.
### EvalCommand
`eval` - \`\`\`java<newline>\`\`\` - Evaluates JShell. If you don't use code blocks, a return is added to the beginning of the code,otherwise, if you are using code blocks, you should return something. (ELEVATED)
### FloodCommand
`flood` - [num] <message> - Floods this channel with a message.
### FromMorseCommand
`frommorse` - <morse> - Converts morse to text. Use / for spaces between words.
### GuildBalanceGraphCommand
`gbalgraph` - [-d] - Graphs this guild's transactions history, transaction by transaction. Use -d to use dates instead
### GuildConfigCommand
`config` - <key> <value> - Configure the bot for this guild. No arguments to view the configuration.
### HeapDumpCommand
`heapdump` - <boolean> - Dumps the heap. (ELEVATED)
### HelpCommand
`help` - <no parameters>  - This screen.
### InfoCommand
`info` - <no parameters>  - Returns various bot statistics.
### InviteBotCommand
`invite` - <no parameters>  - Generates an invite link to invite the bot.
### JoinOrderCommand
`joinorder` - <no parameters>  - Sends this guild's join order.
### LoadoutCommand
`loadout` - <no parameters>  - Points to ultimate-bravery.
### LotteryCommand
`lottery` - [amount] - Enter the lottery! They last for four hours.
### ManageRolesCommand
```
usage: roles [-i] [--deactivate] [-a] [-r] [-b] [params [params ...]]

positional arguments:
  params                 the rest of the parameters for previous arguments

named arguments:
  -i, --init             initialize role management
  --deactivate           remove role management
  -a, --add-role         adds a role
  -r, -d, --remove-role  removes a role
  -b, --rebuild          rebuilds reaction role message
```
### MathCommand
`math` - <math> - Executes math. See <https://github.com/uklimaschewski/EvalEx#supported-operators> for what you can do.
### MessagePurgerCommand
```
usage: purge [-m MESSAGE_IDS] [-a AUTHORS] [-c CHANNEL]

Purges message between (inclusive) by two  messages IDs. There won't be any
message existence  checks,  just  if  the  message  falls  between  the two
provided IDs. Be careful with this! There are no guardrails.

named arguments:
  -m MESSAGE_IDS, --message-ids MESSAGE_IDS
                         removes all  messages  between  and  including the
                         IDs. comma separate, i.e. first,last
  -a AUTHORS, --authors AUTHORS
                         only  remove  authors  from  this  comma-separated
                         user ID list
  -c CHANNEL, --channel CHANNEL
                         remove messages in this  channel,  if not provided
                         current channel will be used
```
### NetherCommand
`nether` - <x1> <z1> - Returns the nether coordinates given a Minecraftian point.
### NowCommand
`now` - <no parameters>  - Returns the current time.
### OverworldCommand
`overworld` - <x1> <z1> - Returns the overworld coordinates given a Minecraftian point.
### PickCommand
`pick` - [num] <options...> - Picks num from a list of options. If num is not specified, it will only pick one. Separate your words with 'or' or a space.
### PingCommand
`ping` - <no parameters>  - Returns the current websocket and API ping.
### PlotCommand
`plot` - <expression> - Plots a function, use x
### PollCommand
`poll` - <"poll title"> <"poll type (single or multiple)"> <"questsion 1"> ["question 2"] ... - Create single-choice and multiple-choice anonymous polls.
### QuoteCommand
```
usage: quote [-s] [-i] [-a] [-d] [-e] [quoteAndAuthor [quoteAndAuthor ...]]

Retrieves a random quote. If fulltext search  fails, it falls back to SQL'S
ILIKE, and changes the embed color to be a little darker.

positional arguments:
  quoteAndAuthor         quote content and quote attribution to search by

named arguments:
  -s, --stats            retrieve stats instead
  -i, --include-author   include who added the quote
  -a, --all              get all quotes by a guild, skipping quoteAndAuthor
  -d, --id               include the internal quote ID
  -e, --exact            search exactly (by default  it performs a fulltext
                         search on quote and quote author)
```
### RemindmeCommand
`remindme` - <duration> <reminder> - Creates a reminder. It is accurate give or take a second.
### RemoveCommandCommand
`removecommand` - <command> - Removes a command from the command handler. Persists until the bot restarts. (ELEVATED)
### RockPaperScissorsCommand
`rps` - <rock/paper/scissors> - Chooses a random, Rock, Paper, or Scissors
### RussianRouletteCommand
`roulette` - <no parameters>  - Kick a random person from your voice channel!
### ServerStatusCommand
`status` - <address[:port]> - Fetches information about a Minecraft server.
### SnowflakeCommand
`snowflake` - <snowflakes...> - Converts Discord snowflake ID(s) to dates.
### SqlCommand
`sql` - <sql query> - Evaluates SQL.
### StatisticsCommand
`stats` - <no parameters>  - Returns command and other statistics.
### StockCommand
```
usage: stock [-g {true,false}] [-y {true,false}] ticker

Grabs stock information from a ticker.

positional arguments:
  ticker                 ticker to fetch

named arguments:
  -g {true,false}, --graph {true,false}
                         generate a stock graph
  -y {true,false}, --yearly {true,false}
                         generated graph will show yearly data
```
### TarkovCommand
`tarkov` - <item name> - Returns pricing information about a given item from the tarkov-market API.
### TeXCommand
`tex` - <tex> - Generates a mathematical equation using TeX markup.
### TeamsCommand
`teams` - <no parameters>  - sends a message with your voice channel divided into teams
### ToMorseCommand
`tomorse` - <string> - Encodes a message to morse code.
### TransactionsCommand
`txns` - <no parameters>  - Fetches your last 20 transactions.
### TransferCommand
`transfer` - <to> <amount> [memo] - Transfer money.
### UnitsCommand
`units` - <conversion> - Launches a conversion between two units separated by 'to'.
### UploadLogsCommand
`uploadlogs` - <no parameters>  - Uploads this bot session's logs to the current channel (ELEVATED)
### UrbanDictionaryCommand
`ud` - <word> - Gets a definition from Urban Dictionary's API.
### UserInfoCommand
`userinfo` - [mentions] - Returns information about a user.
### VersionCommand
`version` - <no parameters>  - Returns the bot's version.
### VideoPokerCommand
`poker` - <ante (with range 100 <= x <= 500 )> - Play video poker to win some money! For the paytable, visit https://en.wikipedia.org/w/index.php?title=Video_poker&oldid=1094389801#Jacks_or_Better
### WeatherCommand
`w` - <location> - Returns the current weather of a location.
### WeatherForecastCommand
`wf` - <location> - Returns the weather forecast of a location.
### WhoHasCommand
`whohas` - <role ID> - Returns who has a certain role in the guild.
### WorkCommand
`work` - <no parameters>  - Work for money every day.

# Regex Listeners
### BetRegexable
`\bbet((s)?|(ting)?|(ted))\b`
### DadRegexable
`^(i['’]?)m(ma)?\b`
### RedditRegexable
`https://reddit.app.link/\w.*`
### RedditShareRegexable
`https://(www.)?reddit.com/r/\w{3,21}/s/\w+(?=/$|$)`
### TeXRegexable
`\$\$(.*?)\$\$`
### TwitterRegexable
`https://(www.)?(twitter|x).com/\w{1,45}/status/\d+`
