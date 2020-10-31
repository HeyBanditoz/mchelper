# Commands
There are a total of 42 commands and 4 regex listeners.
### AddquoteCommand
`addquote` - "<quote>" <author> - Adds a quote to the database.
### BashCommand
`bash` - <bash> - Executes bash if it is installed on system. (ELEVATED)
### CoinFlipCommand
`flip` - <no parameters>  - Flips a coin.
### CoordCommand
`coords` - <save|add,show|list,delete|remove,help> - Saves coordinates to the database. See !coords help
### DefaultChannelCommand
`defaultchannel` - (channel ID) - Gets or sets the default channel for this guild.
### DeleteReminderCommand
`delremind` - <id> - Deletes a reminder.
### DiceRollerCommand
`roll` - [dice notation] - Roll some dice following standard dice notation.
### DictionaryCommand
`define` - [num] <word> - Finds the definition of a word using Owlbot's API.
### EangleCommand
`eangle` - <x1> <z1> <x2> <z2> - Returns the elytra flight angle and distance between two Minecraftian coordinate points.
### EvalCommand
`eval` - \`\`\`groovy<newline>\`\`\` - Evaluates Groovy. If you don't use code blocks, a return is added to the beginning of the code,otherwise, if you are using code blocks, you should return something. (ELEVATED)
### FloodCommand
`flood` - [num] <message> - Floods this channel with a message.
### FromMorseCommand
`frommorse` - <morse> - Converts morse to text. Use / for spaces between words.
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
`loadout` - [stats] - Gives a random league champ and loadout. [stats](must be lowercase): ap = Ability Power, ad = Attack Damage, as = Attack Speed, mana = Mana, ar = Armour, crit = Critical damage or chance, hp = Health, mr = Magic Resist, ms = Movement Speed.
### MathCommand
`math` - <math> - Executes math. See <https://github.com/uklimaschewski/EvalEx#supported-operators> for what you can do.
### NetherCommand
`nether` - <x1> <z1> - Returns the nether coordinates given a Minecraftian point.
### OverworldCommand
`overworld` - <x1> <z1> - Returns the overworld coordinates given a Minecraftian point.
### PickCommand
`pick` - [num] <options...> - Picks num from a list of options. If num is not specified, it will only pick one. Separate your words with 'or' or a space.
### PingCommand
`ping` - <no parameters>  - Returns the current websocket and API ping.
### PlotCommand
`plot` - <expression> - Plots a function, use x
### PrefixCommand
`prefix` - (prefix) - Gets or sets the prefix for this guild. (By default, it is a '!' and must be a char.
### QuoteCommand
```
usage: quote [-s] [-i] [-a] [quoteAndAuthor [quoteAndAuthor ...]]

positional arguments:
  quoteAndAuthor         quote content and quote attribution to search by

named arguments:
  -s, --stats            retrieve stats instead
  -i, --include-author   include who added the quote
  -a, --all              get all quotes by a guild, skipping quoteAndAuthor
```
### RemindmeCommand
`remindme` - <duration> <reminder> - Creates a reminder. It is accurate give or take a second.
### RockPaperScissorsCommand
`rps` - <rock/paper/scissors> - Chooses a random, Rock, Paper, or Scissors
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
### TeXCommand
`tex` - <tex> - Generates a mathematical equation using TeX markup.
### TeamsCommand
`teams` - <no parameters>  - sends a message with your voice channel divided into teams
### ToMorseCommand
`tomorse` - <string> - Encodes a message to morse code.
### UnitsCommand
`units` - <conversion> - Launches a conversion between two units separated by 'to'.
### UploadLogsCommand
`uploadlogs` - <no parameters>  - Uploads this bot session's logs to the current channel (ELEVATED)
### UrbanDictionaryCommand
```
usage: ud [-n NUMBER] [word [word ...]]

Gets a definition from Urban Dictionary's API.

positional arguments:
  word                   word to search for

named arguments:
  -n NUMBER, --number NUMBER
                         the result to get (if multiple exist)
```
### UserInfoCommand
`userinfo` - [mentions] - Returns information about a user.
### VersionCommand
`version` - <no parameters>  - Returns the bot's version.
### WeatherStationCommand
`ws` - [hours] - Returns various statistics of my weather station, including a Fahrenheit graph.

# Regex Listeners
### BetRegexable
`\bbet((s)?|(ting)?|(ted))\b`
### DadRegexable
`^(i'?m)\b`
### RedditRegexable
`https://reddit.app.link/\w.*`
### TeXRegexable
`\$\$(.*?)\$\$`