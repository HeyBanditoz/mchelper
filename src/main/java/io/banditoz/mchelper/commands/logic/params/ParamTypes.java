package io.banditoz.mchelper.commands.logic.params;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public final class ParamTypes {
    private ParamTypes() {
    }

    public static final ParamType<String> STRING = new SimpleType<>("text", String.class, OptionType.STRING) {
        @Override
        public String parseText(String token, BindContext ctx) {
            return token;
        }

        @Override
        public String fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsString();
        }
    };

    public static final ParamType<Integer> INTEGER = new SimpleType<>("a whole number", Integer.class, OptionType.INTEGER) {
        @Override
        public Integer parseText(String token, BindContext ctx) throws BindException {
            try {
                return Integer.parseInt(token.replace(",", ""));
            } catch (NumberFormatException ex) {
                throw new BindException(null, "expected a whole number, got `" + token + "`");
            }
        }

        @Override
        public Integer fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsInt();
        }
    };

    public static final ParamType<Long> LONG = new SimpleType<>("a whole number", Long.class, OptionType.INTEGER) {
        @Override
        public Long parseText(String token, BindContext ctx) throws BindException {
            try {
                return Long.parseLong(token.replace(",", ""));
            } catch (NumberFormatException ex) {
                throw new BindException(null, "expected a whole number, got `" + token + "`");
            }
        }

        @Override
        public Long fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsLong();
        }
    };

    public static final ParamType<Double> DOUBLE = new SimpleType<>("a number", Double.class, OptionType.NUMBER) {
        @Override
        public Double parseText(String token, BindContext ctx) throws BindException {
            try {
                return Double.parseDouble(token.replace(",", ""));
            } catch (NumberFormatException ex) {
                throw new BindException(null, "expected a number, got `" + token + "`");
            }
        }

        @Override
        public Double fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsDouble();
        }
    };

    private static final Pattern PLAIN_DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    public static final ParamType<BigDecimal> DECIMAL = new SimpleType<>("a number", BigDecimal.class, OptionType.NUMBER) {
        @Override
        public BigDecimal parseText(String token, BindContext ctx) throws BindException {
            String cleaned = token.replace(",", "").replace("$", "");
            if (!PLAIN_DECIMAL.matcher(cleaned).matches()) {
                throw new BindException(null, "expected a number, got `" + token + "`");
            }
            return new BigDecimal(cleaned);
        }

        @Override
        public BigDecimal fromOption(OptionMapping option, BindContext ctx) {
            return BigDecimal.valueOf(option.getAsDouble());
        }
    };

    private static final List<String> TRUE_WORDS = List.of("true", "yes", "y", "1", "on");
    private static final List<String> FALSE_WORDS = List.of("false", "no", "n", "0", "off");

    public static final ParamType<Boolean> BOOLEAN = new SimpleType<>("yes or no", Boolean.class, OptionType.BOOLEAN) {
        @Override
        public Boolean parseText(String token, BindContext ctx) throws BindException {
            String lower = token.toLowerCase();
            if (TRUE_WORDS.contains(lower)) {
                return true;
            }
            if (FALSE_WORDS.contains(lower)) {
                return false;
            }
            throw new BindException(null, "expected yes or no, got `" + token + "`");
        }

        @Override
        public Boolean fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsBoolean();
        }
    };

    public static final ParamType<User> USER = new SimpleType<>("a user", User.class, OptionType.USER) {
        @Override
        public User parseText(String token, BindContext ctx) throws BindException {
            Long id = parseUserId(token);
            if (id != null) {
                User cached = ctx.jda().getUserById(id);
                if (cached != null) {
                    return cached;
                }
                try {
                    return ctx.jda().retrieveUserById(id).complete();
                } catch (Exception ex) {
                    throw new BindException(null, "no user with ID `" + id + "` could be found.");
                }
            }
            if (ctx.guild() == null) {
                throw new BindException(null, "expected a user mention or ID, got `" + token + "`");
            }
            return findMemberByName(ctx.guild(), token).getUser();
        }

        @Override
        public User fromOption(OptionMapping option, BindContext ctx) {
            return option.getAsUser();
        }
    };

    public static final ParamType<Member> MEMBER = new SimpleType<>("a member", Member.class, OptionType.USER) {
        @Override
        public Member parseText(String token, BindContext ctx) throws BindException {
            Guild guild = requireGuild(ctx);
            Long id = parseUserId(token);
            if (id == null) {
                return findMemberByName(guild, token);
            }
            Member cached = guild.getMemberById(id);
            if (cached != null) {
                return cached;
            }
            try {
                return guild.retrieveMemberById(id).complete();
            } catch (Exception ex) {
                throw new BindException(null, "no member with ID `" + id + "` in this guild.");
            }
        }

        @Override
        public Member fromOption(OptionMapping option, BindContext ctx) throws BindException {
            User user = option.getAsUser();
            Member member = requireGuild(ctx).getMember(user);
            if (member == null) {
                throw new BindException(null, "`" + user.getName() + "` is not a member of this guild.");
            }
            return member;
        }
    };

    /**
     * @param token The raw token, as typed.
     * @return The ID from a raw ID or a {@code <@id>}/{@code <@!id>} mention, or null if the token isn't shaped
     * like one, meaning it should be treated as a name instead.
     * @throws BindException If the token is all digits but too long to be an ID.
     */
    @Nullable
    private static Long parseUserId(String token) throws BindException {
        String trimmed = token.replaceAll("[<@!>]", "");
        if (trimmed.isEmpty() || !trimmed.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            throw new BindException(null, "`" + token + "` is not a valid user ID.");
        }
    }

    /**
     * Finds the one member going by a name, checking nicknames before usernames.
     *
     * @throws BindException If no member, or more than one, goes by that name.
     */
    private static Member findMemberByName(Guild guild, String token) throws BindException {
        List<Member> byNick = guild.getMembersByEffectiveName(token, true);
        if (byNick.size() == 1) {
            return byNick.getFirst();
        }
        List<Member> byName = guild.getMembersByName(token, true);
        if (byName.size() == 1) {
            return byName.getFirst();
        }
        if (byNick.size() > 1 || byName.size() > 1) {
            throw new BindException(null, "`" + token + "` matches more than one member. Mention them instead.");
        }
        throw new BindException(null, "no member named `" + token + "` in this guild.");
    }

    private static Guild requireGuild(BindContext ctx) throws BindException {
        if (ctx.guild() == null) {
            throw new BindException(null, "a member can only be given in a guild.");
        }
        return ctx.guild();
    }

    /**
     * Binds an enum constant, case-insensitively. Each constant is registered as a Discord choice, so slash
     * users get a dropdown instead of a free text field they can typo.
     *
     * @param <E> The enum being bound.
     */
    public static final class EnumType<E extends Enum<E>> implements ParamType<E> {
        private final Class<E> type;

        public EnumType(Class<E> type) {
            this.type = type;
        }

        @Override
        public String id() {
            return "one of " + names();
        }

        @Override
        public Class<E> javaType() {
            return type;
        }

        @Override
        public E parseText(String token, BindContext ctx) throws BindException {
            for (E constant : type.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(token)) {
                    return constant;
                }
            }
            throw new BindException(null, "expected one of " + names() + ", got `" + token + "`");
        }

        @Override
        public OptionType optionType() {
            return OptionType.STRING;
        }

        @Override
        public E fromOption(OptionMapping option, BindContext ctx) throws BindException {
            return parseText(option.getAsString(), ctx);
        }

        private String names() {
            StringBuilder sb = new StringBuilder();
            E[] constants = type.getEnumConstants();
            for (int i = 0; i < constants.length; i++) {
                if (i > 0) {
                    sb.append(i == constants.length - 1 ? " or " : ", ");
                }
                sb.append('`').append(constants[i].name().toLowerCase()).append('`');
            }
            return sb.toString();
        }
    }

    /**
     * @param type The enum to bind.
     * @param <E>  The enum type.
     * @return A {@link ParamType} binding that enum case-insensitively, offering each constant as a Discord
     * choice.
     */
    public static <E extends Enum<E>> ParamType<E> enumOf(Class<E> type) {
        return new EnumType<>(type);
    }

    /** Boilerplate shared by the built-ins. */
    private abstract static class SimpleType<T> implements ParamType<T> {
        private final String id;
        private final Class<T> javaType;
        private final OptionType optionType;

        SimpleType(String id, Class<T> javaType, OptionType optionType) {
            this.id = id;
            this.javaType = javaType;
            this.optionType = optionType;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Class<T> javaType() {
            return javaType;
        }

        @Override
        public OptionType optionType() {
            return optionType;
        }
    }
}
