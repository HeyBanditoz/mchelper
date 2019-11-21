# MCHelper

MCHelper is a simple Discord bot with a small set of commands, such as
TeX parsing, a math command, dice roller, weather, among a few others.

The bot is currently private. If for whatever reason you would like to
use it, you can build the bot with `./gradlew shadowJar` assuming you
have a Java development environment set up.

## Commands
Some commands may be missing from here. This is out of laziness.
* !roll \<dice notation> - Rolls a dice according to standard dice notation. The parser
is very primitive, and might break. "2d20" will roll two dice with 20
sides.
* !define \<word> - Uses the terrible Owlbot dictionary API to look up a word
for you.
* !eangle \<x1> \<z1> \<x2> \<z2> - Returns an elytra flight angle
and distance between two Mineraftian coordinate points.
* !info - Returns various bot statistics.
* !frommorse \<morse> - Parses a morse encoded message. Use / to separate
word spaces.
* !tomorse \<string> - Convert any message into morse code with the
format explained above. Non alphanumeric characters (excluding comma,
period, question mark.)
are excluded.
* !nether \<x1> \<z1> - Returns the nether coordinates given a set of
Overworld Minecraftian points.
* !overworld \<x1> \<z1> - Does the opposite.
* !rg \<location> - Returns a set of geographic coordinates of a
location using OpenStreetMap.
* !tex \<TeX> - Parses TeX and uploads an image of an equation.
* !pick \[num] \<options...> Accepts a space separated or 'or' separated 
list of options and picks 'num' random options from that list. If 'num' 
is not specified, it will only pick one.
* !units \<conversion> - Performs a unit conversion using GNU units.
Check their docs for more information.
* !weather \<location> - Looks up the weather for any given location
using the DarkSky API.
* !flip - Flips a coin. Call it in the air.

### Commands requiring elevated permissions
Don't give these to any old regular person, they could steal your token
and nuke on the host system, etc etc.
* !eval \<js> - Evaluates JavaScript.
* !bash \<bash> - Evaluates Bash.


## Listeners
The bot also implements a set of a few Regex listeners, for making
lives easier. This includes
* $$TeX Math$$ - Parses text wrapped inside of the '$$'
* https://reddit.app.link/foobar - Grabs a real reddit link from 
reddit.app.link.

## Configuration
Should be mostly self-explanatory. On first run the bot will generate
one for you.

```json
{
  "discordToken": "Discord token goes here",
  "botOwners": [
    "These can run elevated commands, be careful! Put their ID here."
  ],
  "darkSkyAPI": "Dark Sky API key.",
  "owlBotToken": "OwlBot API key."
}
```
