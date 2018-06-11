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

/*
 * History:
 * 2010-10-24 pela Added SELECT tag
 * 2010-10-27 pela Added support of IllegalFormat exception so that its error message is displayed
 * 2010-10-31 pela Changed ServletOutputStream to PrintWriter to support UTF-8 and encodings properly and 
 *                 the printActions method now emits a %20 where attribute names includes the space character.
 * 2010-11-09 pela Added support of 'command' attribute type when generating HTML
 * 2010-11-12 pela Added support of 'options' attribute type when generating HTML
 */
package nu.nethome.home.items.web.servergui;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.ModelAttribute;
import nu.nethome.home.item.*;
import nu.nethome.home.items.web.servergui.attributes.*;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class EditItemPage extends PortletPage {

    protected static final String CANCEL_BUTTON_TEXT = "Cancel";
    protected static final String SAVE_BUTTON_TEXT = "Save";
    protected static final String CATEGORY_MAP[] = {"", "blank_icon_medium",
            "Lamps", "lamp_icon_medium", "Remotes", "remote_icon_medium",
            "Thermometers", "thermometer_icon_medium", "Ports",
            "port_icon_medium", "System", "system_icon_medium", "Timers",
            "timer_icon_medium", "Hardware", "hardware_icon_medium", "GUI",
            "gui_icon_medium", "Gauges", "gauge_icon_medium", "Controls",
            "control_icon_medium"};
    private static final String UNACTIVATED_ITEM_NAME_PREFIX = "#";
    public static final String UPDATE_ATTRIBUTES_ACTION = "update_attributes";
    public static final String DELETE_RENAME_ACTION = "delete_rename";
    public static final String APPLY_BUTTON_TEXT = "Apply";
    protected HomeService server;
    protected String bridgeBrokerId;
    protected String pageName = "edit";
    protected Map<String, AttributeTypePrinterInterface> attributeHandlers = new HashMap<>();
    private SelectClassPage selectClassPage;
    private CreationEventCache creationEventCache;
    private EditPermission editPermission;

    public EditItemPage(String mLocalURL, HomeService server, String mediaDirectory, CreationEventCache creationEvents, EditPermission allowEdit) {
        super(mLocalURL);
        this.server = server;
        this.creationEventCache = creationEvents;
        bridgeBrokerId = findServerInstanceId();
        initiateAttributePlugins(mediaDirectory);
        selectClassPage = new SelectClassPage(mLocalURL, server, mediaDirectory, creationEvents);
        this.editPermission = allowEdit;
    }

    private void initiateAttributePlugins(String mediaDirectory) {
        addAttributePlugin(new StringAttributePrinter());
        addAttributePlugin(new TextAttributePrinter());
        addAttributePlugin(new BooleanAttributePrinter());
        addAttributePlugin(new StringListAttributePrinter());
        addAttributePlugin(new CommandAttributePrinter(this.server));
        addAttributePlugin(new ValueAttributePrinter(this.server));
        addAttributePlugin(new OptionsAttributePrinter(this.server));
        addAttributePlugin(new PasswordAttributePrinter());
        addAttributePlugin(new ItemAttributePrinter(this.server));
        addAttributePlugin(new ItemsAttributePrinter(this.server));
        addAttributePlugin(new StringsAttributePrinter(this.server));
        addAttributePlugin(new MediaFileAttributePrinter(mediaDirectory));
        addAttributePlugin(new DurationAttributePrinter());
    }

    private void addAttributePlugin(AttributeTypePrinterInterface attributePlugin) {
        attributeHandlers.put(attributePlugin.getTypeName(), attributePlugin);
    }

    private String findServerInstanceId() {
        List<DirectoryEntry> names = this.server.listInstances("");
        for (DirectoryEntry directoryEntry : names) {
            // Open the instance so we know class and category
            HomeItemProxy item = this.server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            if (model.getClassName().equals("HomeServer")) {
                return item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE);
            }
        }
        return "";
    }

    @Override
    public List<String> getJavaScriptFileNames() {
        List<String> scripts = new ArrayList<>();
        scripts.add("web/home/js/jquery-1.4.3.min.js");
        scripts.add("web/home/edititempage.js");
        scripts.addAll(selectClassPage.getJavaScriptFileNames());
        return scripts;
    }

    public String getPageName() {
        return "Create/Edit";
    }

    public String getPageNameURL() {
        return pageName;
    }

    /**
     * This is the main entrance point of the class. This is called when a http
     * request is routed to this servlet.
     */
    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        configureServletResponse(res);

        EditItemArguments pageArguments = new EditItemArguments(req);

        if (isClassSelectionPage(pageArguments)) {
            selectClassPage.printPage(req, res, server);

        } else {
            HomeItemProxy item = getEditedHomeItemInstance(pageArguments);

            if (!isActivated(item) && pageArguments.isSaveTypeCancel()) {
                cancelCreation(req, res, server, pageArguments, item);
            } else if (!isActivated(item) && !pageArguments.isAction(UPDATE_ATTRIBUTES_ACTION)) {
                printCreateInstancePage(item, pageArguments, res.getWriter());
            } else if (pageArguments.hasAction()) {
                performActionsAndRedirect(req, res.getWriter(), pageArguments, item);
            } else {
                printItemEditPage(res.getWriter(), pageArguments, item);
            }
        }
    }

    private void cancelCreation(HttpServletRequest req, HttpServletResponse res, HomeService server, EditItemArguments pageArguments, HomeItemProxy item) throws IOException, ServletException {
        this.server.removeInstance(pageArguments.getName());
        if (pageArguments.hasReturnPage()) {
            printRedirectionScript(res.getWriter(),
                    url("name", item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)).gotoReturnPage(pageArguments).toString());
        } else {
            selectClassPage.printPage(req, res, server);
        }
    }

    private boolean isActivated(HomeItemProxy item) {
        return !item.getAttributeValue("Name").startsWith(UNACTIVATED_ITEM_NAME_PREFIX);
    }

    private boolean isClassSelectionPage(EditItemArguments pageArguments) {
        return pageArguments.isAction("select_class")
                || (!pageArguments.hasName() && !pageArguments.hasClassName());
    }

    private void printItemEditPage(PrintWriter p, EditItemArguments pageArguments, HomeItemProxy item) throws ServletException, IOException {
        List<HomeItemError> homeItemErrors = new ArrayList<>();
        printItemEditColumnStart(p);
        HomeItemModel model = item.getModel();
        String name = item.getAttributeValue("Name");
        printItemHeading(p, name, model);

        List<Attribute> attributes = item.getAttributeValues();
        attributes.add(new IDAttribute(item.getAttributeValue("ID")));

        if (countReadonlyAttributes(attributes) > 0) {
            printReadOnlyAttributes(p, attributes.iterator());
        }

        // Print any general errors
        boolean bSet = false;
        for (HomeItemError homeItemError : homeItemErrors) {
            if (homeItemError.type == HomeItemError.ErrorType.general) {
                if (!bSet) {
                    p.println("<div class=\"homeitem-errors\">");
                    bSet = true;
                }
                p.println(homeItemError.getErrorMessage() + "<br/>");
            }
            if (bSet)
                p.println("</div>");
        }

        p.println("<br>");

        // Print Actions
        printActions(p, item, pageArguments);

        // Print Writable Attributes
        printWritableAttributes(p, item, attributes.iterator(), true, homeItemErrors, pageArguments.isEditMode(), pageArguments.getReturnPage(), pageArguments.getReturnSubpage());

        // End of the Item info section
        p.println("</div>");

        // Print the Delete and Rename buttons
        if (editPermission.isEditPermitted()) {
            printFooterSection(p, name);
        }

        // Print page end
        printColumnEnd(p);

        printRelatedItems(p, pageArguments, item);
    }

    private void printRelatedItems(PrintWriter p, EditItemArguments pageArguments, HomeItemProxy item) throws ServletException, IOException {

        this.printRelatedItemColumnStart(p);

        List<DirectoryEntry> relatedItems = server.listInstances("@related=" + item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
        List<String> referring = new ArrayList<>();
        List<String> containing = new ArrayList<>();

        for (DirectoryEntry relatedItem : relatedItems) {
            if (relatedItem.getCategory().equals("Infrastructure")) {
                containing.add(Long.toString(relatedItem.getInstanceId()));
            } else {
                referring.add(Long.toString(relatedItem.getInstanceId()));
            }
        }

        if (referring.size() > 0) {
            this.printRoom(p, pageArguments.getPage(), pageArguments.getName(), "Related Items", null, null, referring.toArray(new String[referring.size()]), server, false, null);
        }

        if (containing.size() > 0) {
            this.printRoom(p, pageArguments.getPage(), pageArguments.getName(), "Located in", null, null, containing.toArray(new String[containing.size()]), server, false, null);
        }

        this.printColumnEnd(p);
    }

    protected void printRelatedItemColumnStart(PrintWriter p) {
        p.println("<div class=\"itemcolumn related\">");
    }


    private void configureServletResponse(HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
    }

    private void performActionsAndRedirect(HttpServletRequest req, PrintWriter p, EditItemArguments arguments, HomeItemProxy item) throws ServletException, IOException {
        List<HomeItemError> homeItemErrors = new ArrayList<>();
        if (arguments.isAction(UPDATE_ATTRIBUTES_ACTION)) {
            processAttributeUpdateAction(req, p, arguments, item, homeItemErrors);
        } else if (arguments.isAction(DELETE_RENAME_ACTION)) {
            processDeleteRenameAction(p, arguments, item);
        } else {
            printReturnToThisPageRedirectionScript(p, arguments, item);
        }
    }

    private void processDeleteRenameAction(PrintWriter p, EditItemArguments arguments, HomeItemProxy item) throws ServletException, IOException {
        if (arguments.isItemDelete()) {
            this.server.removeInstance(arguments.getName());
            printSelectClassPage(p, arguments);
        } else if (arguments.isItemMove() && arguments.hasNewLocation()) {
            placeItemInLocation(item, arguments);
            printReturnToThisPageRedirectionScript(p, arguments, item);
        }
    }

    private void processAttributeUpdateAction(HttpServletRequest req, PrintWriter p, EditItemArguments arguments, HomeItemProxy item, List<HomeItemError> homeItemErrors) {
        if (!arguments.isSaveTypeCancel()) {
            updateAttributes(item, req, arguments, homeItemErrors);
            saveItems(homeItemErrors);
        }
        if (arguments.hasReturnPage() && arguments.isSaveTypeThatReturns()) {
            printRedirectionScript(p,
                    url("name", item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)).gotoReturnPage(arguments).toString());
        } else {
            printReturnToThisPageRedirectionScript(p, arguments, item);
        }
    }

    private void printReturnToThisPageRedirectionScript(PrintWriter p, EditItemArguments arguments, HomeItemProxy item) {
        printRedirectionScript(p,
                url("name", item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE))
                        .preserveEditMode(arguments)
                        .addParameter("page", pageName)
                        .preserveReturnPage(arguments).toString());
    }

    private HomeItemProxy getEditedHomeItemInstance(EditItemArguments arguments) {
        HomeItemProxy item;
        if (arguments.isAction("create") && arguments.hasClassName()) {
            item = createInstanceWithNewName(arguments.getClassName());
            initItemAttributes(item, arguments);
        } else {
            item = this.server.openInstance(arguments.getName()); // NYI Error check
        }
        return item;
    }

    private void initItemAttributes(HomeItemProxy item, EditItemArguments arguments) {
        if (arguments.hasEvent()) {
            Event receivedEvent = creationEventCache.getItemEvent(arguments.getEventId()).getEvent();
            Event initEvent = createInitEvent(receivedEvent, item);
            Object itemImpl = item.getInternalRepresentation();
            if ((itemImpl != null) && (itemImpl instanceof HomeItem)) {
                ((HomeItem) itemImpl).receiveEvent(initEvent);
            }
        }
    }

    private Event createInitEvent(Event receivedEvent, HomeItemProxy item) {
        Event result = new InternalEvent("Init");
        for (String attName : receivedEvent.getAttributeNames()) {
            if (!attName.equals(Event.EVENT_TYPE_ATTRIBUTE)) {
                result.setAttribute(attName, receivedEvent.getAttribute(attName));
            } else {
                result.setAttribute("OriginalType", receivedEvent.getAttribute(attName));
            }
        }
        result.setAttribute("InitId", item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
        return result;
    }

    private void placeItemInLocation(HomeItemProxy item, EditItemArguments arguments) {
        HomeItemProxy location = server.openInstance(arguments.getNewLocation());
        if (location == null) {
            return;
        }
        String itemsInLocationString = location.getAttributeValue("Items");
        String[] itemsInLocation = itemsInLocationString.split(",");
        for (String itemId : itemsInLocation) {
            if (itemId.equalsIgnoreCase(item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE))) {
                return;
            }
        }
        try {
            location.setAttributeValue("Items", itemsInLocationString +
                    (itemsInLocationString.length() == 0 ? "" : ",") +
                    item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
        } catch (IllegalValueException e) {
            // Nothing to do, just fails
        }
    }

    protected int countReadonlyAttributes(List<Attribute> attributes) {
        int readonlyCount = 0;
        for (Attribute attribute : attributes) {
            if (attribute.isReadOnly()) {
                readonlyCount++;
            }
        }
        return readonlyCount;
    }

    protected void printItemHeading(PrintWriter p, String name, HomeItemModel model) {
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader\">");
        p.println("	<img src=\"web/home/"
                + HomeGUI.itemIcon(model.getCategory(), false) + "\" />");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>" + HTMLEncode.encode(name) + "</li>");
        p.println("   <li class=\"classname\">[<a href=\"http://wiki.nethome.nu/doku.php?id="
                + model.getClassName()
                + "\" target=\"new_window\" >"
                + model.getClassName() + "</a>]</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<br>");
    }

    private HomeItemProxy createInstanceWithNewName(String className) {
        HomeItemProxy item;
        HomeItemProxy createdItem;
        int count = 0;
        String newName;
        String newCreatedName;
        // Loop to find a "free" new temporary name
        do {
            newName = "#New Item"
                    + ((count != 0) ? (" " + Integer.toString(count)) : "");
            newCreatedName = "New Item"
                    + ((count != 0) ? (" " + Integer.toString(count)) : "");
            item = server.openInstance(newName);
            createdItem = server.openInstance(newCreatedName);
            count++;
        } while (item != null || createdItem != null);
        // Create the item with a temporary name beginning with "#" to
        // signal that it is not activated
        item = server.createInstance(className, newName);
        return item;
    }

    /**
     * Save all Item configuration to disk
     */
    protected void saveItems(List<HomeItemError> homeItemErrors) {
        HomeItemProxy broker = server.openInstance(bridgeBrokerId);
        try {
            broker.callAction("SaveItems");
        } catch (ExecutionFailure e) {
            homeItemErrors.add(new HomeItemError(e.getMessage()));
        }
    }

    /**
     * Prints the instance creation page presented when a new instance is
     * created. This page allows the user to enter initial values for all
     * attributes that may be initialazed. The instance is already created when
     * we get here.
     *
     * @param item      Instance to initilize
     * @param arguments The current Http request
     * @param p         output stream
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void printCreateInstancePage(HomeItemProxy item,
                                           EditItemArguments arguments, PrintWriter p) throws ServletException,
            IOException {
        // Get the model of the item
        HomeItemModel model = item.getModel();
        String initialName = item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE).replace("#", "");

        // Print static start of page
        printItemEditColumnStart(p);

        // Print page heading
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader\">");
        p.println("	<img src=\"web/home/item.png\" />");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>Create new " + model.getClassName() + "</li>");
        p.println("   <li class=\"classname\">Step 2: Initiate data and activate Item</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<br>");
        p.println("&nbsp;");
        p.println("<form name=\"attributes\" action=\"" + localURL
                + "\" method=\"post\">");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName
                + "\" />");
        if (arguments.hasRoom()) {
            p.println("<input type=\"hidden\" name=\"room\" value=\"" + arguments.getRoom()
                    + "\" />");
        }
        p.println("<input type=\"hidden\" name=\"a\" value=\"update_attributes\" />");
        p.println("<input type=\"hidden\" name=\"name\" value=\""
                + item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\" />");
        if (arguments.isEditMode()) {
            p.println("<input type=\"hidden\" name=\"mode\" value=\"edit\" />");
        }
        if (arguments.hasReturnPage()) {
            p.println("<input type=\"hidden\" name=\"return\" value=\"" + arguments.getReturnPage() + "\" />");
        }
        if (arguments.hasReturnSubpage()) {
            p.println("<input type=\"hidden\" name=\"returnsp\" value=\"" + arguments.getReturnSubpage() + "\" />");
        }

        p.println("<table class=\"actions\">");

        // Print the new instance name input
        printAttribute(p, "Item Name", "new_name", initialName, false);

        // Print the initiable attributes
        Attribute attribute;
        Iterator<Attribute> i = item.getAttributeValues().iterator();
        for (int counter = 0; i.hasNext(); counter++) {
            attribute = i.next();
            if (attribute.isCanInit() && !attribute.isWriteOnly() && !attribute.getType().equals("Hidden")) {
                p.println("<tr>");
                p.println("  <td class=\"attributename\">" + attribute.getName()
                        + "</td>");
                printWritableAttributeValue(p, attribute, counter);
                p.println("</td></tr>");
            }
        }

        // Print Attribute Footer
        p.println("</table>");
        p.println("<br>");

        p.println("<div class=\"footer\">");
        p.print("<input class=\"ibutton\" type=\"submit\" name=\"save_type\" value=\""
                + CANCEL_BUTTON_TEXT + "\"> ");
        p.println("<input class=\"ibutton\" type=\"submit\" name=\"save_type\" value=\"Save Settings and activate\">");
        p.println("</div");
        p.println("</form>");

        // End of the Item info section
        p.println("</div>");

        // Print page end
        printColumnEnd(p);
    }

    /**
     * Prints the class selection page. This is the first step when a user
     * seletcts to create a new instance.
     */
    protected void printSelectClassPage(PrintWriter p, EditItemArguments arguments)
            throws ServletException, IOException {

        // Print static start of page
        printItemEditColumnStart(p);

        // Print page heading
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader\">");
        p.println("	<img src=\"web/home/item.png\" />");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>Create new Item</li>");
        p.println("   <li class=\"classname\">Step 1: Select item type</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<br>");

        // Print class selection box
        p.println("<form name=\"create\" action=\"" + localURL
                + "\" method=\"get\" >");
        p.println("<input type=\"hidden\" name=\"a\" value=\"create\" />");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName
                + "\" />");
        if (arguments.getRoom() != null) {
            p.println("<input type=\"hidden\" name=\"room\" value=\"" + arguments.getRoom()
                    + "\" />");
        }
        if (arguments.hasReturnPage()) {
            p.println("<input type=\"hidden\" name=\"return\" value=\"" + arguments.getReturnPage()
                    + "\" />");
        }
        p.println("<table class=\"actions\">");
        p.println(" <tr>");
        p.println(" <td class=\"actioncolumn\"><input type=\"submit\" value=\"Create new item\"> </td>");
        p.println(" <td><select name=\"class_name\" >");
        p.println("	<option value=\"TCPCommandPort\">- Select Type -</option>");

        // Print all selectable classes
        List<HomeItemInfo> classNames = server.listClasses();
        for (HomeItemInfo classInfo : classNames) {
            p.println("	<option value=\"" + classInfo.getClassName() + "\">" + classInfo
                    + "</option>");
        }
        p.println("	</select>");
        p.println("  </td>");
        p.println(" </tr>");
        p.println("</table>");
        p.println("</div>");
        p.println("</form>");

        // End of the Item info section
        p.println("</div>");

        // Print page end
        printColumnEnd(p);
    }

    protected void printItemEditColumnStart(PrintWriter p) throws ServletException,
            IOException {
        p.println("<div class=\"itemcolumn edit\">");
    }

    /**
     * Update the attribute of the item given the attribute values in the http
     * request. If the instance name begins with "#" we also set "init only"
     * attributes.
     */
    protected void updateAttributes(HomeItemProxy item,
                                    HttpServletRequest req, EditItemArguments arguments, List<HomeItemError> homeItemErrors) {
        List<Attribute> attributes = item.getAttributeValues();
        String newName = arguments.getNewName();
        String room = arguments.getRoom();

        boolean initInstance = !isActivated(item);
        int counter = 0;
        // Loop through all attributes and update those which we got values for
        // and are writable
        for (Attribute attribute : attributes) {
            AttributeTypePrinterInterface parser = getAttributeTypePrinter(attribute.getType());
            try {
                parser.updateAttributeValue(item, attribute, req, initInstance, counter);
            } catch (IllegalValueException e) {
                homeItemErrors.add(new HomeItemError(attribute, e.getMessage()));
            }
            counter++;
        }
        // If a new valid name is specified, rename the instance
        if ((newName != null)
                && !newName.equals(item.getAttributeValue("Name"))) {
            // Try to rename
            try {
                if (server.renameInstance(item.getAttributeValue("Name"),
                        newName) && initInstance) {
                    // Rename OK, and this was an initiation - activate it
                    item.callAction("activate");
                    // If a room was specified, add the new Item to that room
                    if (room != null) {
                        HomeItemProxy roomItem = server
                                .openInstance(room);
                        String items = roomItem.getAttributeValue("Items")
                                + "," + item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE);
                        roomItem.setAttributeValue("Items", items);
                    }
                }
            } catch (IllegalValueException | ExecutionFailure e) {
                homeItemErrors.add(new HomeItemError(e.getMessage()));
            }
        }
    }

    private AttributeTypePrinterInterface getAttributeTypePrinter(String typeName) {
        AttributeTypePrinterInterface result = attributeHandlers.get(typeName);
        if (result == null) {
            result = attributeHandlers.get("String");
        }
        return result;
    }

    /**
     * Print all writable attributes in a form with apply and save buttons
     */
    protected void printWritableAttributes(PrintWriter p,
                                           HomeItemProxy item, Iterator<Attribute> i, boolean includeName,
                                           List<HomeItemError> homeItemErrors, boolean editMode, String returnPage, String returnSubpage) throws ServletException,
            IOException {

        // Print Attribute header
        p.println("<br>");
        p.println("<div class=\"secheader\"><b>Settings</b></div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<form name=\"attributes\" action=\"" + localURL
                + "\" method=\"post\">");
        p.println("<input type=\"hidden\" name=\"a\" value=\"update_attributes\" />");
        p.println("<input type=\"hidden\" name=\"name\" value=\"" + item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE) + "\" />");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName + "\" />");
        if (editMode) {
            p.println("<input type=\"hidden\" name=\"mode\" value=\"edit\" />");
        }
        if (returnPage != null) {
            p.println("<input type=\"hidden\" name=\"return\" value=\"" + returnPage + "\" />");
        }
        if (returnSubpage != null) {
            p.println("<input type=\"hidden\" name=\"returnsp\" value=\"" + returnSubpage + "\" />");
        }
        p.println("<table class=\"actions\">");

        // Print the name attribute
        if (includeName) {
            p.println("<tr>");
            p.println("  <td class=\"attributename\"><i>Item Name</i></td>");
            p.println("  <td><input class=\"iteminput\" type=\"text\" name=\"new_name\" value=\""
                    + HTMLEncode.encode(item.getAttributeValue("Name")) + "\"></td>");
            p.println("</tr>");
        }

        // Print Attributes and values
        Attribute attribute;
        for (
                int counter = 0;
                i.hasNext(); counter++)

        {
            attribute = i.next();
            if (!attribute.isReadOnly() && !attribute.isWriteOnly() && !attribute.getType().equals("Hidden")) {
                p.println("<tr>");
                p.println("  <td class=\"attributename\">" + attribute.getName()
                        + "</td>");
                printWritableAttributeValue(p, attribute, counter);
                printWritableAttributeError(p, homeItemErrors, attribute);
                p.println("</td></tr>");
            }
        }
        p.println("</table>");
        p.println("<br>");
        // Print Attribute Footer
        p.println("<div class=\"footer\">");
        p.print("<input class=\"ibutton\" type=\"submit\" name=\"save_type\" value=\""
                + CANCEL_BUTTON_TEXT + "\"> ");
        if (editPermission.isEditPermitted()) {
            if (returnPage != null) {
                p.println("<input class=\"ibutton\" type=\"submit\" name=\"save_type\" value=\""
                        + SAVE_BUTTON_TEXT + "\"> ");
            }
            p.println("<input class=\"ibutton\" type=\"submit\" name=\"save_type\" value=\""
                    + APPLY_BUTTON_TEXT + "\"> ");
        }
        p.println("</div>");
        p.println("</form>");
    }

    private void printWritableAttributeError(PrintWriter p, List<HomeItemError> homeItemErrors, Attribute attribute) {
        for (HomeItemError homeItemError : homeItemErrors) {
            if (homeItemError.type == HomeItemError.ErrorType.attribute && homeItemError.getAttribute().getName()
                    .equals(attribute.getName())) {
                p.println("  <span class=\"homeitem-error\">"
                        + homeItemError.getErrorMessage() + "</span>");
                break;
            }
        }
    }

    private void printWritableAttributeValue(PrintWriter p, Attribute attribute, int counter) {
        AttributeTypePrinterInterface printer = getAttributeTypePrinter(attribute.getType());
        printer.printAttributeValue(p, attribute, counter);
    }

    private String formatReadOnlyAttributeValue(Attribute attribute) {
        AttributeTypePrinterInterface printer = getAttributeTypePrinter(attribute.getType());
        return printer.attributeToPrintValue(attribute.getValue());
    }

    protected void printAttribute(PrintWriter p, String prettyName,
                                  String htmlName, String value, boolean oddLine)
            throws ServletException, IOException {
        p.println("<tr" + oddLine + ">");
        p.println("  <td class=\"attributename\">" + prettyName + "</td>");
        p.println("  <td><input " + (oddLine ? " class=\"oddline\" " : "")
                + " type=\"text\" name=\"" + htmlName + "\" value=\"" + value
                + "\"></td>");
        p.println("</tr>");
    }

    protected void printReadOnlyAttributes(PrintWriter p, Iterator<Attribute> i)
            throws ServletException, IOException {

        // Print Attribute header
        p.println("<table>");

        // Print Attributes and values
        Attribute attribute;
        while (i.hasNext()) {
            attribute = i.next();
            if (attribute.isReadOnly() && !attribute.isWriteOnly() && !attribute.getType().equals("Hidden")) {
                String unit = attribute.getValue().length() > 0 ? (" " + attribute.getUnit()) : "";
                p.println("<tr>");
                p.println("  <td class=\"attributename\">" + attribute.getName() + ": "
                        + "</td> <td class=\"attributenameandvalue\">" + formatReadOnlyAttributeValue(attribute) + unit + "</td>");
                p.println("</tr>");
            }
        }
        // Print Attribute Footer
        p.println("</table>");
    }

    protected void printActions(PrintWriter p, HomeItemProxy item, EditItemArguments pageArguments)
            throws ServletException, IOException {
        List<Action> actions = item.getModel().getActions();
        if (actions.size() == 0) {
            // No Actions, just return
            return;
        }
        // Print Action Header
        p.println("<div class=\"secheader\"><b>Functions</b></div>");
        p.println("<div class=\"deviderline\"></div>");

        // Print Actions
        p.println("<span class=\"iactions\"><ul>");
        for (Action action : actions) {
            HomeUrlBuilder url = url("page", pageName).addParameter("a", "perform_action")
                    .addParameter("name", item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE))
                    .addParameter("action", action.getName().replace(" ", "%20"))
                    .preserveReturnPage(pageArguments);
            p.print("  <li><a href=\"" + url.toString() + "\">" + action.getName() + "</a></li>");
        }
        p.println("</ul></span>");
    }

    protected void printFooterSection(PrintWriter p, String name)
            throws ServletException, IOException {
        p.println("<div class=\"detail\">");
        p.println("<table class=\"actions\">");
        p.println("<tr>");
        printMoveControl(p, name);
        p.println("</tr>");
        printDeleteControl(p, name);
        p.println("</tr>");
        p.println("</table>");
        p.println("</div>");
    }

    private void printMoveControl(PrintWriter p, String name) {
        p.println("<form name=\"move\" action=\"" + localURL
                + "\" method=\"post\">");
        p.println("<input type=\"hidden\" name=\"name\" value=\"" + HTMLEncode.encode(name)
                + "\" />");
        p.println("<input type=\"hidden\" name=\"a\" value=\"delete_rename\" />");
        p.println("<input type=\"hidden\" name=\"move\" value=\"Place\"/>");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName
                + "\" />");
        p.println(" <td><select name=\"new_location\" onchange=\"this.form.submit()\">");
        printRoomsAsOptions(p, name);
        p.println("</select>");
        p.println("</td>");
        p.println("</form>");
    }

    private void printDeleteControl(PrintWriter p, String name) {
        p.println("<form name=\"delete_rename\" action=\"" + localURL
                + "\" method=\"post\">");
        p.println("<input type=\"hidden\" name=\"name\" value=\"" + HTMLEncode.encode(name)
                + "\" />");
        p.println("<input type=\"hidden\" name=\"a\" value=\"delete_rename\" />");
        p.println("<input type=\"hidden\" name=\"page\" value=\"" + pageName
                + "\" />");
        p.println("<tr>");
        p.println(" <td class=\"actioncolumn\"><input type=\"submit\" name=\"delete\" value=\"Delete "
                + HTMLEncode.encode(name) + "\"></td>");
        p.println("</form>");
    }

    private void printRoomsAsOptions(PrintWriter p, String name) {
        p.println("  <optgroup label=\"Select a Location\">");
        p.println("  <option>[Place " + HTMLEncode.encode(name) + " in Location]</option>");
        List<DirectoryEntry> directoryEntries = server.listInstances("");
        for (DirectoryEntry directoryEntry : directoryEntries) {
            // Open the instance so we know class and category
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            if (model.getCategory().equalsIgnoreCase("Infrastructure")) {
                p.println("  <option value=\""
                        + item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE)
                        + "\""
                        + ">" + HTMLEncode.encode(item.getAttributeValue("Name"))
                        + " [" + model.getClassName() + "]"
                        + "</option>");
            }
        }
        p.println("  </optgroup>");
    }

    @SuppressWarnings("UnusedDeclaration")
    protected String getIconFromCategory(String category) {
        String result = "blank_icon_medium";

        for (int i = 0; i < CATEGORY_MAP.length; i += 2) {
            if (CATEGORY_MAP[i].equals(category)) {
                result = CATEGORY_MAP[i + 1];
            }
        }
        return result;
    }

    private HomeUrlBuilder url(String parameter, String value) {
        return new HomeUrlBuilder(localURL).addParameter(parameter, value);
    }

    private static class IDAttribute implements Attribute {
        final String value;

        public IDAttribute(String value) {
            this.value = value;
        }

        @Override
        public String getName() {
            return "ID";
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public List<String> getValueList() {
            return null;
        }

        @Override
        public String getType() {
            return "String";
        }

        @Override
        public String getUnit() {
            return "";
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public boolean isCanInit() {
            return false;
        }

        @Override
        public boolean isWriteOnly() {
            return false;
        }
    }

}
