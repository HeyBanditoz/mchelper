# MCHelper

MCHelper is a simple Discord bot with a small set of commands, such as
TeX parsing, a math command, dice roller, quotes saving
among a few others. See [the command list](COMMANDS.md) for what you can do.

The bot is currently private. If would like to use it, you can grab the latest jar from CI
[here.](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/raw/build/libs/io.banditoz.mchelper-all.jar?job=package)
Or, use Gradle: `./gradlew shadowJar`

## NOTE ON GATEWAY INTENTS
Currently, the bot requires the server members intent to be enabled. It is
required for catching when users/bots join or leave the guild. See
[GuildMemberJoinEvent](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/events/guild/member/GuildMemberJoinEvent.html)
and [GuildMemberLeaveEvent.](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/events/guild/member/GuildMemberLeaveEvent.html)

## Configuration
On first run the bot will generate one for you. The bot expects
a MariaDB database instance to be available, but it isn't required.
Some functionality will be disabled if the `databaseHostAndPort` field
in the configuration is null or default.
