package io.banditoz.mchelper;

import io.banditoz.mchelper.utils.TeXRenderer;

public class TeXListener extends RegexListener {
    @Override
    protected String regex() {
        return "\\$\\$(.*?)\\$\\$";
    }

    @Override
    public void onMessage(RegexEvent re) {
        if (re.getMatcher().find()) {
            re.getEvent().getChannel().sendTyping().queue();
            String latexString = re.getMatcher().group(1);
            try {
                TeXRenderer.sendTeXToChannel(re.getEvent(), latexString);
            } catch (Exception ex) {
                re.sendExceptionMessage(ex);
            }
        }
    }
}
