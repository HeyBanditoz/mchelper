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

## Quickly getting started
In the repository is a [docker-compose.yml](docker-compose.yml) file you can use to start up a MCHelper instance with a
Postgres-database configured. It will use the latest Docker image from the
[master](https://gitlab.com/HeyBanditoz/mchelper/-/tree/master) branch.

To try it out, you will first need to output the default config to a file:

`docker run --rm registry.gitlab.com/heybanditoz/mchelper:master --entrypoint java -jar /app/bot.jar gensettings > Config.json`

Then edit the resulting `Config.json` file. When you've added your bot token, you can finally run

`docker-compose up`

**Note!** Postgres isn't configured with its data directory to persist. If you remove the container, the database will 
be destroyed.

### Local Development

For development with a database, remove the entire `mchelper` block under `services` in the docker-compose file, and
set up your system environment variables like so:
`HOST=127.0.0.1;DB=postgres;USER=postgres;PASS=SuperSecret;SCHEMA=public`

You will also need to add this under the `postgres` block:
```yaml
ports:
      - 5432:5432/tcp
```

You can access the postgres shell by running `docker exec -u 70 -it postgres psql`.

### Building a Docker Image

The Dockerfile is kept lean to quicken building in CI/CD. You will need to first produce a shadow JAR in the project by
running the shadowJar task either from the IDE or commandline. From the project root, you can run
`DOCKER_BUILDKIT=1 docker build -t mchelper_local .` to build a local MCHelper image, and can reference it accordingly
in Docker commands.

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