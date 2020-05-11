package io.banditoz.mchelper.utils;

import io.banditoz.mchelper.commands.logic.CommandUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeXRenderer {
    /**
     * Render a TeX equation.
     *
     * @param tex The TeX to render.
     * @return A BufferedImage of the equation.
     */
    private static ByteArrayOutputStream renderTeX(String tex) throws IOException {
        // create a formula
        TeXFormula formula = new TeXFormula(tex);

        // render the formula to an icon of the same size as the formula.
        TeXIcon icon = formula
                .createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);

        // insert a border
        icon.setInsets(new Insets(5, 5, 5, 5));

        // now create an actual image of the rendered equation
        BufferedImage image = new BufferedImage(icon.getIconWidth(),
                icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
        g2.drawImage(image, null, 0, 0);

        icon.paintIcon(null, g2, 0, 0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return os;
    }

    public static void sendTeXToChannel(MessageReceivedEvent e, String args) throws Exception {
        String message = "TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator();
        CommandUtils.sendImageReply(message, renderTeX(args), e);
    }
}
