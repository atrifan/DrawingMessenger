package ro.idp.dashboard.ui.components;

import java.awt.geom.Path2D;

/**
 * Created by alexandru.trifan on 03.04.2016.
 */
public class ArrowHead extends Path2D.Float {
    public ArrowHead() {
        moveTo(15, 0);
        lineTo(30, 15);
        lineTo(0, 15);
        lineTo(15, 0);
    }
}
