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

package nu.nethome.home.items.infra;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Plan
 *
 * @author Stefan Stromberg
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Infrastructure")
public class Plan extends HomeItemAdapter implements HomeItem {

    public class PlanItem {

        private static final int IE_Y_OFFSET = 10;
        private static final int IE_X_OFFSET = -40;

        public PlanItem(String itemId) {
            this.itemId = itemId;
            x = -1;
            y = -1;
        }

        private String itemId;
        private int x;
        private int y;

        public String getItemId() {
            return itemId;
        }

        public int getX(boolean isBrowserIE) {
            return x + (isBrowserIE ? IE_X_OFFSET : 0);
        }

        public void setX(int x, boolean isBrowserIE) {
            this.x = x - (isBrowserIE ? IE_X_OFFSET : 0);
        }

        public int getY(boolean isBrowserIE) {
            return y + (isBrowserIE ? IE_Y_OFFSET : 0);
        }

        public void setY(int y, boolean isBrowserIE) {
            this.y = y - (isBrowserIE ? IE_Y_OFFSET : 0);
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"Plan\"  Category=\"Infrastructure\" >"
            + "  <Attribute Name=\"Items\" Type=\"Items\" Get=\"getItems\" 	Set=\"setItems\" />"
            + "  <Attribute Name=\"ImageFile\" Type=\"MediaFile\" Get=\"getImageFile\" 	Set=\"setImageFile\" />"
            + "  <Attribute Name=\"CustomCSS\" Type=\"MediaFile\" Get=\"getCustomCSS\" 	Set=\"setCustomCSS\" />"
            + "  <Attribute Name=\"ItemLocations\" Type=\"String\" Get=\"getItemLocations\" 	Set=\"setItemLocations\" />"
            + "  <Attribute Name=\"UpdateInterval\" Type=\"String\" Get=\"getUpdateInterval\" 	Set=\"setUpdateInterval\" />"
			+ "  <Attribute Name=\"ClickAction\" Type=\"StringList\" Get=\"getClickAction\" 	Set=\"setClickAction\">"
			+ "  <item>Popup</item><item>DefaultAction</item></Attribute>"
            + "</HomeItem> ");

    private Map<String, PlanItem> planItems = null;

    // Public attributes
    protected String items = "";
    protected String imageFile = "media/home.jpg";
    protected String customCSS = "";
    protected String itemLocations = "";
    protected int updateInterval = 2;
    private boolean popupOnClick = false;

    public Plan() {
    }

    public String getModel() {
        return MODEL;
    }

    public void setItemLocation(String itemId, int x, int y, boolean ie) {
        getPlanItems();
        PlanItem itemToMove = planItems.get(itemId);
        if (itemToMove != null) {
            itemToMove.setX(x, ie);
            itemToMove.setY(y, ie);
            recalculateItemAttributes();
        }
    }

    public Collection<PlanItem> getPlanItems() {
        if (planItems == null) {
            createPlanItems();
            locatePlanItems();
            recalculateItemAttributes();
        }
        return planItems.values();
    }

    public void movePlanItem(String id, int newX, int newY) {
        getPlanItems();
        if (planItems.containsKey(id)) {
            PlanItem itemToMove = planItems.get(id);
            itemToMove.setX(newX, false);
            itemToMove.setY(newY, false);
            recalculateItemAttributes();
        }
    }

    private void createPlanItems() {
        planItems = new HashMap<>();
        String[] ids = items.split(",");
        for (String id : ids) {
            HomeItemProxy item = server.openInstance(id);
            if (item != null) {
                PlanItem newPlanItem = new PlanItem(id);
                planItems.put(item.getAttributeValue("ID"), newPlanItem);
            }
        }
    }

    private void locatePlanItems() {
        String[] locations = itemLocations.split(",");
        for (String location : locations) {
            String[] locationParts = location.split("#");
            if (locationParts.length == 3) {
                PlanItem matchingPlanItem = planItems.get(locationParts[0]);
                if (matchingPlanItem != null && planItems.containsKey(locationParts[0])) {
                    try {
                        int x = Integer.parseInt(locationParts[1]);
                        int y = Integer.parseInt(locationParts[2]);
                        planItems.get(locationParts[0]).setX(x, false);
                        planItems.get(locationParts[0]).setY(y, false);
                    } catch (NumberFormatException n) {
                        // ignore
                    }
                }
            }
        }
        assignDefaultLocations();
    }

    private void assignDefaultLocations() {
        int nextY = 32;
        for (PlanItem item : planItems.values()) {
            if (item.getX(false) == -1) {
                item.setX(32, false);
            }
            if (item.getY(false) == -1) {
                item.setY(nextY, false);
                nextY += 32;
            }
        }
    }

    private void recalculateItemAttributes() {
        StringBuilder newItems = new StringBuilder();
        StringBuilder newItemLocations = new StringBuilder();
        boolean isFirstItem = true;
        for (PlanItem item : planItems.values()) {
            if (!isFirstItem) {
                newItems.append(",");
                newItemLocations.append(",");
            }
            newItems.append(item.getItemId());
            newItemLocations.append(item.getItemId());
            newItemLocations.append("#");
            newItemLocations.append(item.getX(isFirstItem));
            newItemLocations.append("#");
            newItemLocations.append(item.getY(false));
            isFirstItem = false;
        }
        this.items = newItems.toString();
        this.itemLocations = newItemLocations.toString();
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
        planItems = null;
    }

    public void addItem(String itemId) {
        removeItem(itemId);
        String temp = items;
        if (!temp.isEmpty()) {
            temp += ",";
        }
        setItems(temp + itemId);
    }

    public void removeItem(String itemId) {
        String[] itemArr = items.split(",");
        String result = "";
        String separator = "";
        for (String item : itemArr) {
            if (!item.equals(itemId)) {
                result += separator;
                result += item;
                separator = ",";
            }
        }
        setItems(result);
    }

    public String getItemLocations() {
        return itemLocations;
    }

    /**
     * @param itemLocations The m_ItemLocations to set.
     */
    public void setItemLocations(String itemLocations) {
        this.itemLocations = itemLocations;
        planItems = null;
    }

    /**
     * @return Returns the m_ImageFile.
     */
    public String getImageFile() {
        return imageFile;
    }

    /**
     * @param ImageFile The m_ImageFile to set.
     */
    public void setImageFile(String ImageFile) {
        imageFile = ImageFile;
    }

    /**
     * @return Returns the m_UpdateInterval.
     */
    public String getUpdateInterval() {
        return Integer.toString(updateInterval);
    }

    /**
     * @param UpdateInterval The m_UpdateInterval to set.
     */
    public void setUpdateInterval(String UpdateInterval) {
        updateInterval = Integer.parseInt(UpdateInterval);
    }

    public int getUpdateIntervalInt() {
        return updateInterval;
    }

    /**
     * @return Returns the m_Attribute5.
     */
    public String getClickAction() {
        return popupOnClick ? "Popup" : "DefaultAction";
    }

    /**
     * @param action The m_Attribute5 to set.
     */
    public void setClickAction(String action) {
        popupOnClick = !action.equals("DefaultAction");
    }

    public boolean isPopupOnClick() {
        return popupOnClick;
    }

    public String getCustomCSS() {
        return customCSS;
    }

    public void setCustomCSS(String customCSS) {
        this.customCSS = customCSS;
    }
}
