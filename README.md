# MCHelper

MCHelper is a simple Discord bot with a small set of commands, such as
TeX parsing, a math command, dice roller, quotes saving
among a few others. See [the command list](COMMANDS.md) for what you can do.

The bot is currently private. If for whatever reason you would like to
use it, you can build the bot with `./gradlew shadowJar` assuming you
have a Java development environment set up. Or, you can check the
CI/CD and see if there are any built.

## NOTE ON GATEWAY INTENTS
By default, the bot will assume you have the server members intent
checked. If not, it will fail to start (for now.) It is required for
the guild leave/join listener to function.

## Configuration
Should be mostly self-explanatory. On first run the bot will generate
one for you. The bot expects a MariaDB database instance to be
available, but it isn't required. Some functionality will be
disabled if the `databaseHostAndPort` field in the configuration
is null or is default.
