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

package nu.nethome.home.items.web.servergui.attributes;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class DurationAttributePrinter extends StringAttributePrinter {
    private static final int S_PER_MINUTE = 60;
    private static final int S_PER_HOUR = (S_PER_MINUTE * 60);
    private static final int S_PER_DAY = (S_PER_HOUR * 24);

    public String getTypeName() {
        return "Duration";
    }

    @Override
    public String attributeToPrintValue(String value) {
        try {
            long duration = Long.parseLong(value);
            long days = duration / S_PER_DAY;
            duration = duration - days * S_PER_DAY;
            long hours = duration / S_PER_HOUR;
            duration = duration - hours * S_PER_HOUR;
            long minutes = duration / S_PER_MINUTE;
            duration = duration - minutes * S_PER_MINUTE;
            long seconds = duration;
            String result = (days != 0) ? Long.toString(days) + " days " : "";
            result += (hours != 0) ? Long.toString(hours) + " hours " : "";
            result += Long.toString(minutes) + " minutes ";
            result += (days == 0) ? Long.toString(seconds) + " seconds" : "";
            return result;
        } catch (NumberFormatException e) {
            return "";
        }
    }
}