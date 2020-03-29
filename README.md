# MCHelper

MCHelper is a simple Discord bot with a small set of commands, such as
TeX parsing, a math command, dice roller, weather, among a few others.

The bot is currently private. If for whatever reason you would like to
use it, you can build the bot with `./gradlew shadowJar` assuming you
have a Java development environment set up. Or, you can check the
CI/CD and see if there are any built.

## NOTE ON GATEWAY INTENTS
By default, the bot will assume you have the server members intent
checked. If not, it will fail to start (for now.) It is required for
the guild leave/join listener to function.

## Listeners
The bot also implements a set of a few Regex listeners, for making
lives easier. This includes
* $$TeX Math$$ - Parses text wrapped inside of the '$$'
* https://reddit.app.link/foobar - Grabs a real reddit link from 
reddit.app.link.

## Configuration
Should be mostly self-explanatory. On first run the bot will generate
one for you.


