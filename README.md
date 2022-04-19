# MCHelper

[![Pipeline Status](https://gitlab.com/HeyBanditoz/mchelper/badges/master/pipeline.svg)](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/file/build/reports/tests/test/index.html?job=test)
[![Coverage Report](https://gitlab.com/HeyBanditoz/mchelper/badges/master/coverage.svg)](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/file/build/reports/jacoco/test/html/index.html?job=test)

[Latest Test Report](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/file/build/reports/tests/test/index.html?job=test)
|
[Latest Coverage Report](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/file/build/reports/jacoco/test/html/index.html?job=test)

MCHelper is a simple Discord bot with a small set of commands, such as TeX parsing, a math command, dice roller, quotes
saving among a few others. See [the command list](COMMANDS.md) for what you can do.

It can also save data to a Postgres-configured database.

The bot is currently private. If you would like to use it, you can grab the latest jar from CI
[here.](https://gitlab.com/HeyBanditoz/mchelper/-/jobs/artifacts/master/raw/build/libs/io.banditoz.mchelper-all.jar?job=package)
Or, use Gradle: `./gradlew shadowJar`

## NOTE ON GATEWAY INTENTS

Currently, the bot requires the server members intent to be enabled. It is required for catching when users/bots join or
leave the guild. See
[GuildMemberJoinEvent](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/events/guild/member/GuildMemberJoinEvent.html)
and [GuildMemberLeaveEvent.](https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/events/guild/member/GuildMemberLeaveEvent.html)

## Configuration

On first run the bot will generate one for you. All external services the bot can access will be configured here, except
the Postgres database. To configure that, you'll need to pass the following environment variables to Java:
* HOST
* DB
* USER
* PASS
* SCHEMA

An example of a systemd unit file is available in the repository [here.](mchelper.service)

To set up and configure Postgres, refer to the Ubuntu guide [here. (Ubuntu.)](https://www.postgresql.org/download/linux/ubuntu/)

## Documentation

Some documentation is available in the [documentation folder.](docs)