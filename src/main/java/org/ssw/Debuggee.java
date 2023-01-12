package org.ssw;

import java.util.ArrayList;
import java.util.List;

public class Debuggee {
    public static void main(String[] args) {
        int n = 10;
        final List<String> result = new ArrayList<>();
        final int[] arr = {2, 3, 6, 2};

        for (int i = 0; i < n; i++) {
            String line = "";
            for (int j = n - i; j > 1; j--) {
                line += " ";
            }
            for (int j = 0; j <= i; j++) {
                line += "* ";
            }

            result.add(line);
        }

        for (final String line : result) {
            System.out.println(line);
        }
    }
}
