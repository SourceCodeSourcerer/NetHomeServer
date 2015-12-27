/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class CommandLineParser {

    private String line = "";
    private final static String delimiter = ",";

    public CommandLineParser() {
    }

    public void addToken(String token) {
        if (line.length() > 0) {
            line += delimiter;
        }
        line += quote(token);
    }

    public String getLine() {
        return line;
    }

    public static List<String> parseLine(String searchText) {
        List<String> result = new ArrayList<>();
        if (!searchText.isEmpty()) {
            for (String token : searchText.split(",")) {
                result.add(unQuote(token));
            }
        }
        if (searchText.endsWith(",")) {
            result.add("");
        }
        return result;
    }

    /**
     * Use to determine if a particular word entered in the
     * search box should be discarded from the search.
     */
    public static String unQuote(String token) {
        String result = token.replace("%2C", ",");
        result = result.replace("%0D", "\r");
        result = result.replace("%0A", "\n");
        result = result.replace("%25", "%");
        result = result.replace("%00", "");
        return result;
    }

    public static String quote(String token) {
        String result = token.replace("%", "%25");
        result = result.replace(",", "%2C");
        result = result.replace("\r", "%0D");
        result = result.replace("\n", "%0A");
        return result;
    }
} 