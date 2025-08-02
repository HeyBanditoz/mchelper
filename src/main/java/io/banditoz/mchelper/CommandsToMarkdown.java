//package io.banditoz.mchelper;
//
//import io.avaje.config.Config;
//import io.banditoz.mchelper.commands.logic.Command;
//import io.banditoz.mchelper.regexable.Regexable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//public class CommandsToMarkdown {
//    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsToMarkdown.class);
//
//    public static void commandsToMarkdown() throws Exception {
//        // TODO there must be a better way of doing this...
//        Config.setProperty("mchelper.owlbot.token", "temp");
//        Config.setProperty("mchelper.finnhub.token", "temp");
//        Config.setProperty("mchelper.darksky.token", "temp");
//        LOGGER.info("Beginning dumping of commands and regex listeners to markdown...");
//        MCHelper mcHelper = new MCHelperTestImpl();
//        StringBuilder markdown = new StringBuilder("# Commands\n");
//        ClassNameComparator comp = new ClassNameComparator();
//
//        List<Command> commands = new ArrayList<>(mcHelper.getCommandHandler().getCommands());
//        commands.sort(comp);
//        List<Regexable> regexables = new ArrayList<>(mcHelper.getRegexListeners());
//        regexables.sort(comp);
//
//        markdown.append("There are a total of ").append(commands.size()).append(" commands and ")
//                .append(regexables.size()).append(" regex listeners.\n");
//
//        for (Command command : commands) {
//            markdown.append("### ").append(command.getClass().getSimpleName());
//            markdown.append("\n");
//            markdown.append(command.getHelp().toString());
//            markdown.append("\n");
//        }
//
//        markdown.append("\n# Regex Listeners\n");
//
//        for (Regexable regexable : regexables) {
//            markdown.append("### ").append(regexable.getClass().getSimpleName()).append('\n');
//            markdown.append("`").append(regexable.getPattern()).append('`');
//            markdown.append('\n');
//        }
//        Path path = null;
//        try {
//            path = Path.of("COMMANDS.md");
//            Files.write(path, markdown.toString().getBytes());
//        } catch (IOException e) {
//            LOGGER.error("Could not write to file!" + path.getFileName(), e);
//            System.exit(1);
//        }
//        LOGGER.info("Dump complete. Saved to " + path.getFileName().toString() + ".");
//    }
//
//    private static class ClassNameComparator implements Comparator<Object> {
//        @Override
//        public int compare(Object o1, Object o2) {
//            return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
//        }
//    }
//}
