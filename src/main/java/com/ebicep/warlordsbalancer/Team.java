package com.ebicep.warlordsbalancer;

import java.util.function.Function;

enum Team {
    RED(Color::red), BLUE(Color::blue);
    public static final Team[] VALUES = values();
    public final Function<Color, String> getColor;

    Team(Function<Color, String> getColor) {
        this.getColor = getColor;
    }
}
