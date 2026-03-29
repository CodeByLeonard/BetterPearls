package com.ranoe.betterPearls.logic;

import org.bukkit.util.Vector;

public class Utils {

    private static Vector hermiteCurve(Vector p0, Vector p1, Vector v0, Vector v1, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double h00 = 2*t3 - 3*t2 + 1;
        double h10 = t3 - 2*t2 + t;
        double h01 = -2*t3 + 3*t2;
        double h11 = t3 - t2;

        return p0.clone().multiply(h00)
                .add(v0.clone().multiply(h10))
                .add(p1.clone().multiply(h01))
                .add(v1.clone().multiply(h11));
    }

    public static Vector calculateCurve(Vector start, Vector end, Vector startDirection, double strength, double t) {
        double distance = start.distance(end);
        Vector v0 = startDirection.clone().normalize().multiply(distance * strength);
        Vector v1 = end.clone().subtract(start).normalize().multiply(distance * strength * 0.5);

        return hermiteCurve(start, end, v0, v1, t);
    }
}
