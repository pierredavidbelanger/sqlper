package ca.pjer.sqlper.support.mapper;

import java.util.Comparator;

public class UpperUnderscoreComparator implements Comparator<String> {

    public static final Comparator<String> INSTANCE = new UpperUnderscoreComparator();

    @Override
    public int compare(String s1, String s2) {

        // somewhat based on String.CASE_INSENSITIVE_ORDER

        int l1 = s1.length();
        int i1 = 0;

        int l2 = s2.length();
        int i2 = 0;

        while (i1 < l1 || i2 < l2) {

            if (i1 >= l1) {
                return -1;
            }
            if (i2 >= l2) {
                return 1;
            }

            char c1 = s1.charAt(i1);
            if (c1 == '_') {
                i1++;
                continue;
            }

            char c2 = s2.charAt(i2);
            if (c2 == '_') {
                i2++;
                continue;
            }

            if (c1 != c2) {
                c1 = Character.toUpperCase(c1);
                c2 = Character.toUpperCase(c2);
                if (c1 != c2) {
                    c1 = Character.toLowerCase(c1);
                    c2 = Character.toLowerCase(c2);
                    if (c1 != c2) {
                        return c1 - c2;
                    }
                }
            }

            i1++;
            i2++;
        }

        return 0;
    }
}
