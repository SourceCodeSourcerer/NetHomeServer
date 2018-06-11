/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.hue;

import org.json.JSONObject;

/**
 *
 */
public class LightState {
    private boolean isOn;
    private int brightness;
    private int hue;
    private int saturation;
    private int colorTemperature;

    public LightState(int brightness, int hue, int saturation) {
        isOn = true;
        this.brightness = brightness;
        this.hue = hue;
        this.saturation = saturation;
        this.colorTemperature = 0;
    }

    public LightState(int brightness, int colorTemperature) {
        isOn = true;
        this.brightness = brightness;
        this.hue = -1;
        this.saturation = -1;
        this.colorTemperature = colorTemperature;
    }

    public LightState() {
        isOn = false;
    }

    public LightState(JSONObject state) {
        isOn = state.getBoolean("on") && state.getBoolean("reachable");
        if (state.has("bri")) {
            brightness = state.getInt("bri");
        } else {
            brightness = isOn ? 100 : 0;
        }
        if (state.has("hue")) {
            hue = state.getInt("hue");
            saturation = state.getInt("sat");
        }
        if (state.has("colormode") && state.getString("colormode").equals("ct")) {
            this.colorTemperature = state.getInt("ct");
        }
    }

    public boolean isOn() {
        return isOn;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getHue() {
        return hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public boolean hasColorTemperature() {
        return colorTemperature > 0;
    }

    public Boolean hasHueSat() {
        return saturation > 0;
    }
}
