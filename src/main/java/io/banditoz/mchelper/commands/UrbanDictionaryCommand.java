package io.banditoz.mchelper.commands;

import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.urbandictionary.UDDefinition;
import io.banditoz.mchelper.urbandictionary.UDResult;
import io.banditoz.mchelper.urbandictionary.UDSearcher;
import io.banditoz.mchelper.utils.Help;
import net.dv8tion.jda.api.EmbedBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.stream.Collectors;

public class UrbanDictionaryCommand extends Command {
    @Override
    public String commandName() {
        return "ud";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParser(getDefualtArgs());
    }

    @Override
    protected void onCommand(CommandEvent ce) throws Exception {
        UDSearcher ud = new UDSearcher(ce.getMCHelper());
        Namespace args = getDefualtArgs().parseArgs(ce.getCommandArgsWithoutName());
        String search = args.getList("word").stream().map(Object::toString).collect(Collectors.joining(" "));
        int num = args.getInt("number") - 1;
        UDResult result = ud.search(search);
        if (result.getResults().isEmpty()) {
            ce.sendReply("No definition found.");
            return;
        }
        UDDefinition definition = result.getResults().get(num);
        String definitionString = formatUrbanDictionaryLinkToMarkdown(definition.getDefinition(), true);
        String definitionExample = formatUrbanDictionaryLinkToMarkdown(definition.getExample(), false);
        String definitionExampleTwo = null;
        if (definitionExample.length() > 1024 ) {
            String originalDef = definitionExample;
            definitionExample = definitionExample.substring(0, 1022);
            definitionExampleTwo = originalDef.substring(1022);
        }

        ce.sendEmbedReply(new EmbedBuilder()
                .setTitle(search, definition.getPermalink())
                .setAuthor(definition.getAuthor() + " ↑" + definition.getThumbsUp() + " ↓" + definition.getThumbsDown())
                .setDescription(definitionString)
                .addField("Example", '*' + definitionExample + '*', true)
                .addField(definitionExampleTwo != null ? "(cont'd)" : null, (definitionExampleTwo != null ? '*' + definitionExampleTwo + '*' : null), true)
                .setTimestamp(definition.getWrittenOn())
                .setFooter(num + 1 + "/" + result.getResults().size())
                .build());
    }

    private String formatUrbanDictionaryLinkToMarkdown(String s, boolean isDef) {
        // TODO rewrite this so it uses some parser thing, because this sucks :)
        String n = s.replaceAll("\\[(.*?)]", "[$1](https://www.urbandictionary.com/define.php?term={$1}");
        while (n.contains("{")) {
            String requiredString = n.substring(n.indexOf("{") + 1, n.indexOf("}")).replace(" ", "%20");
            n = n.replaceFirst("\\{(.*?)}", requiredString + ")");
        }
        if (n.length() > 2048 && isDef) {
            return s.replace("[", "")
                    .replace("]", "");
        }
        else if (n.length() > 1024 && !isDef) {
            return s.replace("[", "")
                    .replace("]", "");
        }
        else {
            return n;
        }
    }

    private ArgumentParser getDefualtArgs() {
        ArgumentParser parser = ArgumentParsers.newFor("ud").addHelp(false).build();
        parser.description("Gets a definition from Urban Dictionary's API.");
        parser.addArgument("-n", "--number")
                .type(Integer.class)
                .setDefault(1)
                .help("the result to get (if multiple exist)");
        parser.addArgument("word")
                .help("word to search for")
                .nargs("*");
        return parser;
    }
}
