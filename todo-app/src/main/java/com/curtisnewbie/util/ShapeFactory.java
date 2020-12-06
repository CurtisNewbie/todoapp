package com.curtisnewbie.util;

import javafx.scene.effect.Bloom;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Factory for shapes
 *
 * @author yongjie.zhuang
 */
public final class ShapeFactory {

    private static final String GREEN_HEX = "#7DA948";
    private static final String RED_HEX = "#E75839";
    private static final int POINT_RADIUS = 2;

    public static Circle greenCircle() {
        Circle p = new Circle();
        p.setRadius(POINT_RADIUS);
        p.setSmooth(true);
        p.setEffect(new Bloom());
        p.setFill(Color.web(GREEN_HEX));
        return p;
    }

    public static Circle redCircle() {
        Circle p = new Circle();
        p.setRadius(POINT_RADIUS);
        p.setSmooth(true);
        p.setEffect(new Bloom());
        p.setFill(Color.web(RED_HEX));
        return p;
    }
}
