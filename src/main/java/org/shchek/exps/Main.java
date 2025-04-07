package org.shchek.exps;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private final static double a = 9.8892;
    private final static double b = -15.3727;
    private final static double c = -1.0537;
    private final static double d = -1.4451;
    private final static double e = 0.6943;
    private final static double le = 0;
    private final static double ri = 2;
    private final static double h = 0.1;

    public static double j(double u) {
        return (d * Math.pow(u, 2) + b * u + e) / (Math.pow(c, 2) + a * Math.pow(u, 4));
    }

    public static void main(String[] args) {
        List<Double> u = new ArrayList<>();
        double dm = 0, dp = 0, omega;
        u.add(le + Math.abs(le - ri) / 2);
        u.add(u.get(u.size() - 1) + h);

        if (j(u.get(u.size() - 2)) >= j(u.get(u.size() - 1))) {
            while (u.get(u.size() - 1) <= ri) {
                u.add(u.get(u.size() - 1) + h);
                if (u.get(u.size() - 1) >= le && u.get(u.size() - 1) <= ri) {
                    dp = j(u.get(u.size() - 1)) - j(u.get(u.size() - 2));
                    dm = j(u.get(u.size() - 2)) - j(u.get(u.size() - 3));
                    if (dp > 0 && dm > 0 && dp + dm > 0) {
                        omega = u.get(u.size() - 2) + (Math.pow(h, 2) * dp - Math.pow(h, 2) * dm) / (2 * (h * dp + h * dm));
                        System.out.println(omega);
                        System.out.println(j(omega));
                        break;
                    }
                } else {
                    if (Math.abs(u.get(u.size() - 1) - a) < Math.abs(u.get(u.size() - 1) - b)) {
                        u.remove(u.size() - 1);
                        omega = le;
                    } else {
                        u.remove(u.size() - 1);
                        omega = ri;
                    }
                    if (j(omega) < j(u.get(u.size() - 1)) && j(omega) < j(u.get(u.size() - 2))) {
                        System.out.println(omega);
                        System.out.println(j(omega));
                    } else if (j(u.get(u.size() - 1)) < j(u.get(u.size() - 2))) {
                        System.out.println(u.get(u.size() - 1));
                        System.out.println(j(u.get(u.size() - 1)));
                    } else {
                        System.out.println(u.get(u.size() - 2));
                        System.out.println(j(u.get(u.size() - 2)));
                    }
                    break;
                }
            }
        } else {
            while (u.get(u.size() - 1) >= le) {
                u.add(u.get(u.size() - 1) - h);
                if (u.get(u.size() - 1) >= le && u.get(u.size() - 1) <= ri) {
                    dp = j(u.get(u.size() - 3)) - j(u.get(u.size() - 2));
                    dm = j(u.get(u.size() - 2)) - j(u.get(u.size() - 1));
                    if (dp > 0 && dm > 0 && dp + dm > 0) {
                        System.out.println((Math.pow(h, 2) * dm - Math.pow(h, 2) * dp));
                        System.out.println((2 * (h * dm + h * dp)));
                        omega = u.get(u.size() - 2) + (Math.pow(h, 2) * dm - Math.pow(h, 2) * dp) / (2 * (h * dm + h * dp));
                        System.out.println(omega);
                        System.out.println(j(omega));
                        break;
                    }
                } else {
                    if (Math.abs(u.get(u.size() - 1) - a) < Math.abs(u.get(u.size() - 1) - b)) {
                        u.remove(u.size() - 1);
                        omega = le;
                    } else {
                        u.remove(u.size() - 1);
                        omega = ri;
                    }
                    if (j(omega) < j(u.get(u.size() - 1)) && j(omega) < j(u.get(u.size() - 2))) {
                        System.out.println(omega);
                        System.out.println(j(omega));
                    } else if (j(u.get(u.size() - 1)) < j(u.get(u.size() - 2))) {
                        System.out.println(u.get(u.size() - 1));
                        System.out.println(j(u.get(u.size() - 1)));
                    } else {
                        System.out.println(u.get(u.size() - 2));
                        System.out.println(j(u.get(u.size() - 2)));
                    }
                    break;
                }
            }
        }

    }
}