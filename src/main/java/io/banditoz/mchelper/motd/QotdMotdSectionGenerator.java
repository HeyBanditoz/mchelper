package io.banditoz.mchelper.motd;

import io.banditoz.mchelper.MCHelper;

import java.awt.Color;

public class QotdMotdSectionGenerator extends AbstractQotdMotdSectionGenerator {
    public QotdMotdSectionGenerator(MCHelper mcHelper) {
        super(mcHelper, false, Color.GREEN);
    }
}
