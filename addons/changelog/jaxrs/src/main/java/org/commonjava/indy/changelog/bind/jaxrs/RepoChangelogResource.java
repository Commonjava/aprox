/**
 * Copyright (C) 2013~2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.changelog.bind.jaxrs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.commonjava.indy.bind.jaxrs.IndyResources;
import org.commonjava.indy.changelog.cache.RepoChangelogCache;
import org.commonjava.indy.changelog.conf.RepoChangelogConfiguration;
import org.commonjava.indy.changelog.model.RepositoryChangeLog;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.subsys.infinispan.CacheHandle;
import org.commonjava.indy.util.ApplicationContent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api( value = "Indy Repository change logs searching" )
@Path( "/api/repo/changelog" )
@ApplicationScoped
public class RepoChangelogResource
        implements IndyResources
{
    @Inject
    @RepoChangelogCache
    private CacheHandle<String, RepositoryChangeLog> changeLogCache;

    @Inject
    private RepoChangelogConfiguration config;

    @ApiOperation( "Retrieve change logs by specified store key" )
    @ApiResponses( { @ApiResponse( code = 404, message = "Change logs are not available" ),
                           @ApiResponse( code = 200, response = String.class,
                                         message = "Change logs for store key" ) } )
    @GET
    @Path( "/{packageType}/{type: (hosted|group|remote)}/{name}" )
    @Produces( ApplicationContent.application_json )
    public Response getChangelogByStoreKey(
            final @ApiParam( required = true ) @PathParam( "packageType" ) String packageType,
            final @ApiParam( required = true ) @PathParam( "type" ) String type,
            final @ApiParam( required = true ) @PathParam( "name" ) String name, @Context final UriInfo uriInfo )
    {
        if ( !config.isEnabled() )
        {
            return Response.status( 404 ).entity( "{\"error\":\"Change log module not enabled\"}" ).build();
        }
        StoreKey key = new StoreKey( packageType, StoreType.valueOf( type ), name );
        return Response.status( 200 ).entity( getLogsByStoreKey( key ) ).build();
    }

    @ApiOperation( "Retrieve all change logs" )
    @ApiResponses( { @ApiResponse( code = 404, message = "Change logs are not available" ),
                           @ApiResponse( code = 200, response = String.class,
                                         message = "change logs for store key" ) } )
    @GET
    @Path( "all" )
    @Produces( ApplicationContent.application_json )
    public Response getAllChangelogs()
    {
        if ( !config.isEnabled() )
        {
            return Response.status( 404 ).entity( "{\"error\":\"Change log module not enabled\"}" ).build();
        }
        return Response.status( 200 ).entity( getAllLogs() ).build();
    }

    private List<RepositoryChangeLog> getLogsByStoreKey( StoreKey storeKey )
    {
        return changeLogCache.execute( c -> c.values()
                                             .stream()
                                             .filter( ch -> ch.getStoreKey().equals( storeKey ) )
                                             .collect( Collectors.toList() ) );
    }

    private List<RepositoryChangeLog> getAllLogs()
    {
        return changeLogCache.execute( c -> new ArrayList<>( c.values() ) );
    }
}
