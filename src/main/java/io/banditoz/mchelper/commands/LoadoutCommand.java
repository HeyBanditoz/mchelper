package io.banditoz.mchelper.commands;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Map;
import com.merakianalytics.orianna.types.common.Platform;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.champion.ChampionRotation;
import com.merakianalytics.orianna.types.core.staticdata.*;
import io.banditoz.mchelper.MCHelper;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;
import io.banditoz.mchelper.utils.TwoDimensionalPoint;
import io.banditoz.mchelper.utils.database.Database;
import io.banditoz.mchelper.utils.database.GuildData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public class LoadoutCommand extends Command {
    private static ArrayList<Champion> championArrayList;
    private static ArrayList<Item> itemArrayList;
    private static ArrayList<String> role;
    private static ArrayList<ReforgedRuneSlot> domination, inspiration, precision, resolve, sorcery;
    private static LocalDateTime lastUpdate;
    private static boolean canRun = true;
    @Override
    public String commandName() {
        return "loadout";
    }

    @Override
    public Help getHelp() {
        return new Help(commandName(), false).withParameters("<save|add,show|list,delete|remove,help>")
                .withDescription("Gives a random league champ and loadout.");
    }

    public static void createData() {
        try {
            Champions champions = Champions.withRegion(Region.NORTH_AMERICA).get();
            championArrayList = new ArrayList<>();
            championArrayList.addAll(champions);

            Items items = Items.withRegion(Region.NORTH_AMERICA).get();
            itemArrayList = new ArrayList<>();
            itemArrayList.addAll(items);

            role = new ArrayList<>();
            role.add("Top");
            role.add("Middle");
            role.add("ADC");
            role.add("Support");
            role.add("Jungle");

            ReforgedRunes runes = ReforgedRunes.withRegion(Region.NORTH_AMERICA).get();
            domination = new ArrayList<>();
            domination.addAll(runes.getTree().getDomination());
            inspiration = new ArrayList<>();
            inspiration.addAll(runes.getTree().getInspiration());
            precision = new ArrayList<>();
            precision.addAll(runes.getTree().getPrecision());
            resolve = new ArrayList<>();
            resolve.addAll(runes.getTree().getResolve());
            sorcery = new ArrayList<>();
            sorcery.addAll(runes.getTree().getSorcery());
            lastUpdate = LocalDateTime.now();
        } catch (Exception ex ) {
            canRun = false;
            return;
        }
        canRun = true;
    }

    @Override
    protected void onCommand(CommandEvent ce) {
        if (!canRun) {
            ce.sendReply("Orianna could not be reached. Try again in a moment.");
            Thread thread = new Thread(LoadoutCommand::createData);
            thread.start();
            return;
        }
        int itemCount = 5;
        StringBuilder builder = new StringBuilder();
        builder.append("You will be playing ***");
        Champion champ = championArrayList.get((int)(Math.random() * (championArrayList.size())));
        builder.append(champ.getName());
        builder.append("*** and building: \n");
        ArrayList<String> done = new ArrayList<>();
        while (true) {
            Item item = itemArrayList.get((int)(Math.random() * (itemArrayList.size())));
            if (!item.exists()
                    || item.getBuildsInto() != null
                    || !item.getMaps().contains(Map.SUMMONERS_RIFT)
                    || !item.getTags().contains("Boots")
            ) {
                continue;
            }
            builder.append("***");
            builder.append(item.getName());
            builder.append("***\n");
            break;
        }
        if (champ.getName().contains("Viktor")) {
            itemCount = 4;
            builder.append("Hex Core Upgrades***\n");
        }
        for (int i = 0; i<5; i++) {
            Item item = itemArrayList.get((int)(Math.random() * (itemArrayList.size())));
            if (!item.exists()
                    || item.getBuildsInto() != null
                    || !item.getMaps().contains(Map.SUMMONERS_RIFT)
                    || item.getTags().contains("Trinket")
                    || item.getTags().contains("Boots")
                    || done.contains(item.getName())
                    || item.getName().contains("Enchantment")
                    || item.getKeywords().contains("Ornn")
                    || item.getKeywords().contains("ornn")
                    || item.getName().contains("Potion")
                    || item.getName().contains("Elixir")
                    || item.getName().contains("Snax")
                    || item.getKeywords().contains("yellow")
                    || item.getName().equalsIgnoreCase("\'Your Cut\'")
                    || item.getSource() != 0
                    || item.getName().contains("Total Biscuit")
                    || item.getName().contains("Death's Daughter")
                    || item.getName().contains("Raise Morale")
                    || item.getName().contains("Fire At Will")
                    || item.getName().contains("Hex Core")
                    || item.getName().contains("Minion Dematerializer")
                    || item.getTags().contains("GoldPer")
                    || item.getName().contains("Black Spear")
                    || item.getName().contains("Control Ward")
                    || item.getName().contains("(Quick Charge)")
            ) {
                i--;
                continue;
            }
            done.add(item.getName());
            builder.append("\\****");
            builder.append(item.getName());
            builder.append("***\n");
        }
        ce.sendReply(builder.toString());

        builder = new StringBuilder();
        builder.append("Your runes will be:\n");

        int primaryInt = (int)(Math.random() * (5)), secondaryInt;
        do {
            secondaryInt = (int)(Math.random() * (5));
        } while (secondaryInt == primaryInt);
        ArrayList<ReforgedRuneSlot> primary = precision, secondary = domination;
        int keystone = 0, slot1 = 0, slot2 = 0, slot3 = 0;
        switch (primaryInt) {
            case 0:
                builder.append("***Precision*** with ***");
                primary = precision;
                keystone = 4;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 1:
                builder.append("***Domination*** with ***");
                primary = domination;
                keystone = 4;
                slot1 = 3;
                slot2 = 3;
                slot3 = 4;
                break;
            case 2:
                builder.append("***Sorcery*** with ***");
                primary = sorcery;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 3:
                builder.append("***Resolve*** with ***");
                primary = resolve;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 4:
                builder.append("***Inspiration*** with ***");
                primary = inspiration;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
        }
        builder.append(primary.get(0).get((int)(Math.random() * (keystone))).getName() + "***, ***");
        builder.append(primary.get(1).get((int)(Math.random() * (slot1))).getName() + "***, ***");
        builder.append(primary.get(2).get((int)(Math.random() * (slot2))).getName() + "***, ***");
        builder.append(primary.get(3).get((int)(Math.random() * (slot3))).getName() + "***.\n Your secondary will be ");

        switch (secondaryInt) {
            case 0:
                builder.append("***Precision*** with ***");
                secondary = precision;
                break;
            case 1:
                builder.append("***Domination*** with ***");
                secondary = domination;
                break;
            case 2:
                builder.append("***Sorcery*** with ***");
                secondary = sorcery;
                break;
            case 3:
                builder.append("***Resolve*** with ***");
                secondary = resolve;
                break;
            case 4:
                builder.append("***Inspiration*** with ***");
                secondary = inspiration;
                break;
        }
        builder.append(secondary.get(1).get((int)(Math.random() * (3))).getName() + "*** and ***");
        builder.append(secondary.get(2).get((int)(Math.random() * (3))).getName() + "***.");
        ce.sendReply(builder.toString());
        LocalDateTime localDateTime = LocalDateTime.now().minus(Duration.ofHours(5));
        if (lastUpdate == null || lastUpdate.isBefore(LocalDateTime.now().minus(Duration.ofMinutes(1)))) {
            Thread thread = new Thread(LoadoutCommand::createData);
            thread.start();
        }
    }
}
