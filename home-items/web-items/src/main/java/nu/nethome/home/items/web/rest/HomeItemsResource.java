/**
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

package nu.nethome.home.items.web.rest;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.web.rest.exceptions.RestException;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nu.nethome.home.items.web.rest.ItemDirectoryEntryDto.toDtos;

@Path("/rest")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class HomeItemsResource {

    public static final int ITEM_NOT_FOUND = 100;
    private HomeService server;
    private LogReader logReader;
    private static final String START_TIME_PARAMETER = "start";
    private static final String STOP_TIME_PARAMETER = "stop";

    public HomeItemsResource(HomeService server) {
        this.server = server;
        logReader = new LogReader(server);
    }

    /**
     * List all HomeItem instances
     *
     * @return a directory listing of all HomeItems running in the server
     */
    @GET
    @Path("/items")
    public List<ItemDirectoryEntryDto> listItems() {
        return toDtos(server.listInstances(""));
    }

    /**
     * Get the content of the specified HomeItem
     *
     * @param itemId Identity of the Item to get
     * @return HomeItem description
     */
    @GET
    @Path("/items/{itemId}")
    public ItemDto getItem(@PathParam("itemId") String itemId) {
        return new ItemDto(validateNotNull(server.openInstance(itemId)));
    }

    /**
     * Update attributes and instance name of a HomeItem
     *
     * @param itemId  Identity of the Item to update
     * @param itemDto Name and attribute values to update
     * @return An updated HomeItem description
     * @throws IllegalValueException
     */
    @PUT
    @Path("/items/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto setAttributes(@PathParam("itemId") String itemId, ItemDto itemDto) throws IllegalValueException {
        return updateAttributesAndName(validateNotNull(server.openInstance(itemId)), itemDto);
    }

    private ItemDto updateAttributesAndName(HomeItemProxy item, ItemDto itemDto) throws IllegalValueException {
        if (itemDto.getAttributes() != null) {
            updateItemAttributes(item, itemDto);
        }
        if (itemDto.getName() != null && !item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE).equals(itemDto.getName())) {
            updateItemName(item, itemDto);
        }
        return new ItemDto(item);
    }

    private void updateItemName(HomeItemProxy item, ItemDto itemDto) {
        if (!server.renameInstance(item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE), itemDto.getName())) {
            throw new RestException("Illegal instance name", 400, HttpURLConnection.HTTP_NOT_MODIFIED);
        }
    }

    private void updateItemAttributes(HomeItemProxy item, ItemDto itemDto) throws IllegalValueException {
        for (AttributeDto attribute : itemDto.getAttributes()) {
            item.setAttributeValue(attribute.getName(), attribute.getValue());
        }
    }

    /**
     * Delete the specified HomeItem
     *
     * @param itemId Identity of the Item to delete
     * @return HomeItem description
     */
    @DELETE
    @Path("/items/{itemId}")
    public void deleteItem(@PathParam("itemId") String itemId) {
        if (!server.removeInstance(itemId)) {
            throw new RestException("Illegal instance name", 900, HttpURLConnection.HTTP_NOT_MODIFIED);
        }
    }

    /**
     * Get log values for the specified time period
     *
     * @param itemId
     * @return log values
     * @throws IOException
     */
    @GET
    @Path("/items/{itemId}/log")
    public List<Object[]> log(@PathParam("itemId") String itemId,
                              @QueryParam(START_TIME_PARAMETER) String startTime,
                              @QueryParam(STOP_TIME_PARAMETER) String stopTime) throws IOException {
        return logReader.getLog(startTime, stopTime, validateNotNull(server.openInstance(itemId)));
    }

    /**
     * Create a new HomeItem instance. Any attribute values not supplied will assume default values from the HomeItem.
     * If the instance name starts with "#", the new instance is not activated and this has to be made later with a
     * call to the "activate"-action.
     *
     * @param itemDto Name, Class and attribute values of the new instance
     * @return HomeItem description
     * @throws IllegalValueException
     * @throws ExecutionFailure
     */
    @POST
    @Path("/items")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto createItem(ItemDto itemDto) throws IllegalValueException, ExecutionFailure {
        HomeItemProxy item = validateCreation(server.createInstance(validateString(itemDto.getClassName()), validateString(itemDto.getName())));
        if (itemDto.getAttributes() != null) {
            updateItemAttributes(item, itemDto);
        }
        if (!itemDto.getName().startsWith("#")) {
            item.callAction("activate");
        }
        return new ItemDto(item);
    }

    private HomeItemProxy validateCreation(HomeItemProxy itemProxy) {
        if (itemProxy == null) throw new RestException("Could not create HomeItem",
                800, HttpURLConnection.HTTP_NOT_ACCEPTABLE);
        return itemProxy;
    }

    private String validateString(String string) {
        if (string == null || string.isEmpty()) throw new RestException("Required argument missing",
                700, HttpURLConnection.HTTP_BAD_REQUEST);
        return string;
    }


    /**
     * Call an action on the specified HomeItem
     *
     * @param itemId Identity of the Item to invoke the action on
     * @param actionName The action to invoke
     * @return HomeItem description of the state of the HomeItem after the action is performed
     * @throws ExecutionFailure
     */
    @POST
    @Path("/items/{itemId}/actions/{actionName}/invoke")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ItemDto performAction(@PathParam("itemId") String itemId, @PathParam("actionName") String actionName) throws ExecutionFailure {
        HomeItemProxy item = validateNotNull(server.openInstance(itemId));
        item.callAction(validateString(actionName));
        return new ItemDto(item);
    }

    private HomeItemProxy validateNotNull(HomeItemProxy homeItemProxy) {
        if (homeItemProxy == null) throw new RestException("No such HomeItem",
                ITEM_NOT_FOUND, HttpURLConnection.HTTP_NOT_FOUND);
        return homeItemProxy;
    }
}
