package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.TeXRenderer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeXListener extends Listener {
    private static Pattern pattern = Pattern.compile("\\$\\$(.*?)\\$\\$");

    @Override
    public void onMessage() {
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            e.getChannel().sendTyping().queue();
            String latexString = m.group(1);
            try {
                TeXRenderer.sendTeXToChannel(e, latexString);
            } catch (Exception ex) {
                sendExceptionMessage(ex);
            }
        }
    }
}
