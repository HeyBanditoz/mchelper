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

For development with a database, remove the entire `mchelper` block under `services` in the docker-compose file.

You will also need to add this under the `postgres` block:
```yaml
ports:
  - 5432:5432/tcp
```

You can access the postgres shell by running `docker exec -u 70 -it postgres psql`.

You'll have to configure database access. In `application.yml`:

```yml
mchelper:
  database:
    url: jdbc:postgres://127.0.0.1/postgres/?currentSchema=public
    username: postgres
    password: SuperSecret
```

### Dependency Injection
MCHelper uses [avaje-inject](https://avaje.io/inject/) to handle dependency injection and overall application lifecycle.
It's very similar to Spring, however much more lightweight, as the act of dependency injection is shifted to
compile-time to actually generate the code to inject the dependencies. Dependency injection was eventually chosen
because the act of managing all the classes for the project became a Sisyphean effort, as `MCHelperImpl` became a
[god object](https://en.wikipedia.org/wiki/God_object) for managing it all.

As for what it does, it takes care of injecting dependencies into classes. Instead of a class reaching out to a huge
object to get a dependency (such as the HTTP clients) it's just injected into the class for you, as a dependency. This
massively promotes separation of concerns and testability overall.

### Commands
MCHelper uses a home-grown framework with support for text-based commands, slash commands, along with cooldowns, 
permissions (bot owner level, via `ElevatedCommand`, guild-level permissions, via `Command#getRequiredPermissions`),
and single-shot button and modal interactions.

If you're looking for a solid base to start a JDA-based Discord bot, this isn't it. You're going to want
Freya022's [BotCommands](https://github.com/freya022/BotCommands) or another fabulous command library for JDA, instead.
Hit up the JDA guild to find one!

MCHelper is not interested in refactoring to use a third-party command framework.

#### Your first command
Let's examine the anatomy of a basic command that interacts with the database.
```java
@Singleton
@RequiresDatabase
public class ThingFetcherCommand extends Command {
    private final ThingDao thingDao;

    @Inject
    public ThingFetcherCommand(ThingDao thingDao) {
        this.thingDao = thingDao;
    }

    @Override
    public String commandName() {
        return "getthing";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false)
                .withParameters("<count>")
                .withDescription("Gets a bunch of things!");
    }

    @Override
    protected Status onCommand(CommandEvent ce) throws Exception {
        int count = Integer.parseInt(ce.getCommandArgsString());
        return handleThings(ce, count);
    }
    
    @Slash
    public Status onSlashCommand(SlashCommandEvent sce,
                                 @Param(desc = "The count of things to fetch.") int count) {
        return handleThings(sce, count);
    }
    
    private Status handleThings(ICommandEvent ce, int count) {
        List<Thing> things = thingDao.getThings(count);
        if (things.isEmpty()) {
            ce.sendReply("There are no things! :(");
            return Status.FAIL;
        }
        ce.sendReply("Here are your things! " + things);
        return Status.SUCCESS;
    }
}
```
Here's how some of the annotations work:
* `@Singleton` -> Marks a Java class as being managed by the dependency injection framework. This makes it available
  to inject into other classes. In the context of a Command, this allows it to be given to the CommandHandler and
  SlashCommandHandler for registration.
* `@Inject` -> Not required, but stylistically denotes a constructor is *asking* for those classes to be passed into it
  by the dependency injection framework.
* `@RequiresDatabase` -> The framework won't attempt to create the command unless the database is configured. All
  corresponding DAO classes should also be annotated this way.
* `@Slash` -> This marks the method as receiving an executed slash command. Due to legacy and design, slash commands
  were bolted on to regular commands to try to centralize logic. Therefore, slash command methods must be annotated with
  * `@Slash` annotated `onSlashCommand` methods, return a `Status` enum, and have at least one parameter, of which the
     first *must* have a `SlashCommandEvent` class.

DAO classes (interactions with the database) live in the `/database/dao` package. Each should have a corresponding
interface and impl. In the future, this "single-interface-single-implementation" (sorry, I know this is silly) pattern
would make it easier to swap out database backends.

Notice how there are three command event classes, `ICommandEvent`, `CommandEvent`, and `SlashCommandEvent`.
`ICommandEvent` is the interface that both other classes inherit from. It contains common behavior applicable to both
text and slash commands. `CommandEvent` comes from text command invocation and contains raw text arguments in various
forms, as well as wrapping the `MessageReceivedEvent`. `SlashCommandEvent` is more barebones but wraps the
`SlashCommandInteractionEvent` from JDA and enables the interface.

You'll want to use `ICommandEvent` for most things, and falling back to the implementations if you need methods
specific to slash commands or text commands.

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
the bot can access will be configured here, including the Postgres database.

An example of a systemd unit file is available in the repository [here.](mchelper.service)
Note that I don't use systemd to manage MCHelper anymore, but rather Docker. It should still work, though.

To set up and configure Postgres, refer to the Ubuntu guide
[here. (Ubuntu.)](https://www.postgresql.org/download/linux/ubuntu/) or alternative guide for your system.

## Tests

The test suite requires a configured Postgres database. By default, the suite will use a Postgres service from
[Testcontainers](https://java.testcontainers.org/) via Docker. If you want to use a normal Postgres installation
instead, configure the following environment variables:
```
NO_DOCKER_POSTGRES=true
AVAJE_PROFILES=override
```
then create a `application-override.yml` file within the [test resources directory.](src/test/resources) Configure the
database as you would normally.

If using a regularly installed Postgres database it's recommended to drop all the tables either when the tests are
finished, or before they run, especially if you're working on database migrations. Tests that use the database
truncate tables and reset sequences between each test, though.