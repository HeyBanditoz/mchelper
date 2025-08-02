package io.banditoz.mchelper.motd;

import java.awt.Color;

import io.avaje.inject.BeanScope;

public class QotdMotdSectionGenerator extends AbstractQotdMotdSectionGenerator {
    public QotdMotdSectionGenerator(BeanScope beanScope) {
        super(beanScope, false, Color.GREEN);
    }
}
