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

To try it out, copy the `application-example.yml` file to `application.yml` editing it with your token. Set your
environment variables for the database (see below for what you need,) then you can run the database migration. This will
create tables on first launch, or run migrations that don't exist yet.

`docker-compose run --entrypoint java -jar /app/bot.jar migrate`

Finally, bring up the bot.

`docker-compose up`

**Note!** Postgres isn't configured with its data directory to persist. If you remove the container, the database will 
be destroyed.

### Local Development

MCHelper uses Java 21.

For development with a database, remove the entire `mchelper` block under `services` in the docker-compose file, and
set up your system environment variables like so:
`HOST=127.0.0.1;DB=postgres;USER=postgres;PASS=SuperSecret;SCHEMA=public`

You will also need to add this under the `postgres` block:
```yaml
ports:
      - 5432:5432/tcp
```

You can access the postgres shell by running `docker exec -u 70 -it postgres psql`.

### Liquibase
MCHelper uses Liquibase to handle database migrations. TODO add more details!

If you want to prevent silent errors where rollbacks or updates don't happen if you ran the initial migration changelogs
(created the initial schema) from the Main class, and then are testing your new changelogs from your dev environment, 
you will  need to create a symbolic link to the SQL directory, like this in Powershell (this requires admin!):

`New-Item -ItemType SymbolicLink -Path "sql" -Target ".\src\main\resources\sql\"`

Or, in Bash:

`ln -s src/main/resources/sql sql`

This is because the file paths from the jar will show up in the `databasechangelog` as `sql/postgres/changelog-file.yml`
(because Java resources) whereas running it locally will show up as
`src/main/resources/sql/postgres/changelog-file.yml`, so the symlink is needed for consistency.

### Prometheus
MCHelper uses [OpenTelemetry](https://opentelemetry.io/) for basic metrics. A
[Prometheus](https://prometheus.io/)-compatible HTTP server is provided and its port can be configured with the
`mchelper.metrics.port` config value. The port is logged on startup. You can scrape this HTTP server using Prometheus or
other compatible metrics solution.

Current recorded events:

* GenericEvents' implementing classes are recorded as
  a [LongCounter](https://javadoc.io/static/io.opentelemetry/opentelemetry-api/1.36.0/io/opentelemetry/api/metrics/LongCounter.html#add(long,io.opentelemetry.api.common.Attributes)).
* The HikariCP SQL connection pool also tracks some basic metrics around how long it takes to acquire a connection,
  how long it takes a connection for, et al., using
  the [opentelemetry-hikaricp-3.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/hikaricp-3.0/library)
  library.
* JVM stats around garbage collection, CPU, et al. are tracked using
  the [opentelemetry-runtime-metrics](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/runtime/jvm-metrics.md)
  library.

You can configure a simple health check using Grafana, using this
[PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/) query:

`sum(increase(jda_events_total{event_name="GatewayPingEvent"}[5m]))`

and alert if the last value drops below 0, for example. This should alert after 5 minutes, plus evaluation time, plus
pending period; if your bot stops receiving heartbeats (perhaps it or Discord is down.)

### Building a Docker Image

The Dockerfile is kept lean to quicken building in CI/CD. You will need to first produce a shadow JAR in the project by
running the shadowJar task either from the IDE or commandline. From the project root, you can run
`DOCKER_BUILDKIT=1 docker build -t mchelper_local .` to build a local MCHelper image, and can reference it accordingly
in Docker commands.

## Configuration

You will need to copy `application-example.yml` to `application.yml` and edit it with your tokens. All external services
the bot can access will be configured here, except the Postgres database. To configure that, you'll need to pass the
following environment variables to the JVM:
* HOST
* DB
* USER
* PASS
* SCHEMA

An example of a systemd unit file is available in the repository [here.](mchelper.service)

To set up and configure Postgres, refer to the Ubuntu guide [here. (Ubuntu.)](https://www.postgresql.org/download/linux/ubuntu/)

## Documentation

Some documentation is available in the [documentation folder.](docs)
