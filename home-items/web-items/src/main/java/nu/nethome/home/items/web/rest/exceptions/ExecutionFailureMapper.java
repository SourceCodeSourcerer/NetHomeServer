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

package nu.nethome.home.items.web.rest.exceptions;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.items.web.rest.exceptions.RestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.HttpURLConnection;

@Provider
public class ExecutionFailureMapper implements ExceptionMapper<ExecutionFailure> {

    @Override
    public Response toResponse(ExecutionFailure exception) {
        return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .entity(exception.getMessage())
                .header(RestException.ERROR_CODE_HEADER, 200)
                .header(RestException.ERROR_MESSAGE_HEADER, "Execution failure")
                .build();
    }
}
