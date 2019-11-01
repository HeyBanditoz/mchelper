package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.TeXRenderer;

public class TeXListener extends RegexListener {
    @Override
    protected String regex() {
        return "\\$\\$(.*?)\\$\\$";
    }

    @Override
    public void onMessage() {
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
