package io.banditoz.mchelper.motd;

import java.awt.Color;

import io.avaje.inject.BeanScope;

public class QotdMotdExcludedSectionGenerator extends AbstractQotdMotdSectionGenerator {
    public QotdMotdExcludedSectionGenerator(BeanScope beanScope) {
        super(beanScope, true, new Color(66, 159, 33));
    }
}
