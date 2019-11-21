package io.banditoz.mchelper.utils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class TeXRenderer {
    /**
     * Render a TeX equation.
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

        icon.paintIcon(null, g2, 0,0);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return os;
    }

    public static void sendTeXToChannel(MessageReceivedEvent e, String args) throws Exception {
        String imageName = MD5.computeMD5(args) + ".png";
        long before = System.currentTimeMillis();
        File f = new File(imageName);

        ByteArrayOutputStream latex = TeXRenderer.renderTeX(args);
        // compress image to oxipng (https://github.com/shssoichiro/oxipng)
        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            latex.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            long after = System.currentTimeMillis() - before;

            e.getMessage().getChannel()
                    .sendMessage("TeX for " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " (took " + after + " ms to generate)")
                    .addFile(f)
                    .queue();
            latex.close();
        }
        finally {
            f.delete();
        }
    }
}
