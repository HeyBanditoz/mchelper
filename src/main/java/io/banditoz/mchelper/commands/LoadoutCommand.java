package io.banditoz.mchelper.commands;

import com.merakianalytics.orianna.types.common.Map;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.staticdata.*;
import io.banditoz.mchelper.commands.logic.Command;
import io.banditoz.mchelper.commands.logic.CommandEvent;
import io.banditoz.mchelper.utils.Help;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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
        return new Help(commandName(), false).withParameters("[stats]")
                .withDescription("Gives a random league champ and loadout. [stats](must be lowercase): ap = Ability Power, ad = Attack Damage, as = Attack Speed, mana = Mana, ar = Armour, crit = Critical damage or chance, hp = Health, mr = Magic Resist, ms = Movement Speed.");
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
        } catch (Exception ex) {
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
        ArrayList<String> strings = new ArrayList<>();
        if (ce.getCommandArgs().length > 1) {
            ArrayList<String> stats = new ArrayList<>(Arrays.asList("ap", "ad", "as", "mana", "ar", "crit", "hp", "mr", "ms"));
            strings.addAll(Arrays.asList(ce.getCommandArgs()[1].split(",")));
            ArrayList<String> removable = new ArrayList<>();
            for (String s : strings) {
                if (!stats.contains(s)) {
                    removable.add(s);
                    ce.sendReply("Your parameter " + s + " has been removed because it was not found as a valid stat, use !help.");
                }
            }
            strings.removeAll(removable);
            if (strings.isEmpty()) {
                strings = new ArrayList<>();
            }
        }
        int itemCount = 5;
        StringBuilder builder = new StringBuilder();
        builder.append("You will be playing ***");
        Champion champ = championArrayList.get((int) (Math.random() * (championArrayList.size())));
        builder.append(champ.getName());
        builder.append("*** and building: \n");
        ArrayList<String> done = new ArrayList<>();
        int count = 0;
        while (true) {
            count++;
            Item item = itemArrayList.get((int) (Math.random() * (itemArrayList.size())));
            if (!item.exists()
                    || item.getBuildsInto() != null
                    || !item.getMaps().contains(Map.SUMMONERS_RIFT)
                    || !item.getTags().contains("Boots")
            ) {
                continue;
            }
            builder.append("\\* ").append(item.getName()).append('\n');
            break;
        }
        if (champ.getName().contains("Viktor")) {
            itemCount = 4;
            builder.append("Hex Core Upgrades***\n");
        }
        for (int i = 0; i < 5; i++) {
            Item item = itemArrayList.get((int) (Math.random() * (itemArrayList.size())));
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
                    || item.getName().contains("Mikael's Crucible")
                    || item.getName().contains("Fire at Will")
            ) {
                i--;
                continue;
            }
            if (!strings.isEmpty()) {
                boolean breakout = true;
                if (strings.contains("ap") && item.getStats().getAbilityPower() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("ad") && item.getStats().getAttackDamage() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("as") && item.getStats().getPercentAttackSpeed() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("mana") && item.getStats().getMana() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("ar") && item.getStats().getArmor() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("crit") && (item.getStats().getCriticalStrikeDamage() > 0.0 || item.getStats().getCriticalStrikeChance() > 0.0)) {
                    breakout = false;
                }
                if (strings.contains("hp") && item.getStats().getHealth() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("mr") && item.getStats().getMagicResist() > 0.0) {
                    breakout = false;
                }
                if (strings.contains("ms") && item.getStats().getPercentMovespeed() > 0.0) {
                    breakout = false;
                }
                if (breakout) {
                    i--;
                    continue;
                }
            }
            done.add(item.getName());
            builder.append("\\* ").append(item.getName()).append("\n");
            if (count>=1000) {
                ce.sendReply("Items could not be found for you selected filters, please try again.");
                return;
            }
        }
        builder.append("Your runes will be:\n");

        int primaryInt = (int) (Math.random() * (5)), secondaryInt;
        do {
            secondaryInt = (int) (Math.random() * (5));
        } while (secondaryInt == primaryInt);
        ArrayList<ReforgedRuneSlot> primary = precision, secondary = domination;
        int keystone = 0, slot1 = 0, slot2 = 0, slot3 = 0;
        switch (primaryInt) {
            case 0:
                builder.append("***Precision*** with ");
                primary = precision;
                keystone = 4;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 1:
                builder.append("***Domination*** with ");
                primary = domination;
                keystone = 4;
                slot1 = 3;
                slot2 = 3;
                slot3 = 4;
                break;
            case 2:
                builder.append("***Sorcery*** with ");
                primary = sorcery;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 3:
                builder.append("***Resolve*** with ");
                primary = resolve;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
            case 4:
                builder.append("***Inspiration*** with ");
                primary = inspiration;
                keystone = 3;
                slot1 = 3;
                slot2 = 3;
                slot3 = 3;
                break;
        }
        builder.append(primary.get(0).get((int) (Math.random() * (keystone))).getName()).append(", ");
        builder.append(primary.get(1).get((int) (Math.random() * (slot1))).getName()).append(", ");
        builder.append(primary.get(2).get((int) (Math.random() * (slot2))).getName()).append(", ");
        builder.append(primary.get(3).get((int) (Math.random() * (slot3))).getName()).append(".\n Your secondary will be ");

        switch (secondaryInt) {
            case 0:
                builder.append("***Precision*** with ");
                secondary = precision;
                break;
            case 1:
                builder.append("***Domination*** with ");
                secondary = domination;
                break;
            case 2:
                builder.append("***Sorcery*** with ");
                secondary = sorcery;
                break;
            case 3:
                builder.append("***Resolve*** with ");
                secondary = resolve;
                break;
            case 4:
                builder.append("***Inspiration*** with ");
                secondary = inspiration;
                break;
        }
        builder.append(secondary.get(1).get((int) (Math.random() * (3))).getName()).append(" and ");
        builder.append(secondary.get(2).get((int) (Math.random() * (3))).getName()).append(".");
        ce.sendReply(builder.toString());
        LocalDateTime localDateTime = LocalDateTime.now().minus(Duration.ofHours(5));
        if (lastUpdate == null || lastUpdate.isBefore(LocalDateTime.now().minus(Duration.ofMinutes(1)))) {
            Thread thread = new Thread(LoadoutCommand::createData);
            thread.setName("Orianna");
            thread.start();
        }
    }
}
