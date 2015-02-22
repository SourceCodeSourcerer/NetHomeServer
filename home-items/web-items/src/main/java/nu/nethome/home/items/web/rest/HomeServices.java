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

import nu.nethome.home.items.web.rest.exceptions.ExecutionFailureMapper;
import nu.nethome.home.items.web.rest.exceptions.RestExceptionMapper;
import nu.nethome.home.system.HomeService;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class HomeServices extends Application {
    private static Set services = new HashSet();
    private static HomeService server;

    public  HomeServices() {
        services.add(new HomeItemsResource(server));
        services.add(new RestExceptionMapper());
        services.add(new ExecutionFailureMapper());
    }

    @Override
    public  Set getSingletons() {
        return services;
    }

    public  static Set getServices() {
        return services;
    }

    public static void setServer(HomeService server) {
        HomeServices.server = server;
    }
}
