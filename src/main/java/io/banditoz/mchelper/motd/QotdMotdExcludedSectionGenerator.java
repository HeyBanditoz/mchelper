package io.banditoz.mchelper.motd;

import io.banditoz.mchelper.MCHelper;

import java.awt.Color;

public class QotdMotdExcludedSectionGenerator extends AbstractQotdMotdSectionGenerator {
    public QotdMotdExcludedSectionGenerator(MCHelper mcHelper) {
        super(mcHelper, true, new Color(66, 159, 33));
    }
}
