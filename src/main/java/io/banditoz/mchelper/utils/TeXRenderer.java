package io.banditoz.mchelper.utils;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeXRenderer {
    /**
     * Render a TeX equation.
     * @param tex The TeX to render.
     * @return A BufferedImage of the equation.
     */
    public static ByteArrayOutputStream renderTeX(String tex) throws IOException {
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
}
