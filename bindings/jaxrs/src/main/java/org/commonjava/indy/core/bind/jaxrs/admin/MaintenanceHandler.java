/**
 * Copyright (C) 2011-2020 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.core.bind.jaxrs.admin;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.commonjava.indy.IndyWorkflowException;
import org.commonjava.indy.bind.jaxrs.IndyResources;
import org.commonjava.indy.bind.jaxrs.util.REST;
import org.commonjava.indy.bind.jaxrs.util.ResponseHelper;
import org.commonjava.indy.core.ctl.ContentController;
import org.commonjava.indy.core.ctl.IspnCacheController;
import org.commonjava.indy.data.StoreDataManager;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Api( value="Maintenance", description = "Basic repository maintenance functions" )
@Path( "/api/admin/maint" )
@REST
public class MaintenanceHandler
    implements IndyResources
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private ContentController contentController;

    @Inject
    private StoreDataManager storeDataManager;

    @Inject
    private IndyObjectMapper mapper;

    @Inject
    private ResponseHelper responseHelper;

    @ApiOperation( "[Deprecated] Rescan all content in the specified repository to re-initialize metadata, capture missing index keys, etc." )
    @ApiResponse( code = 200, message = "Rescan was started successfully. (NOTE: There currently is no way to determine when rescanning is complete.)" )
    @Path( "/rescan/{type: (hosted|group|remote)}/{name}" )
    @GET
    @Deprecated
    public Response deprecatedRescan( @ApiParam( value = "The type of store / repository", allowableValues = "hosted,group,remote", required=true ) final @PathParam( "type" ) String type,
                            @ApiParam( "The name of the store / repository" ) @PathParam( "name" ) final String name )
    {
        final StoreType storeType = StoreType.get( type );

        String altPath = Paths.get( "/api/admin/maint", MAVEN_PKG_KEY, type, name).toString();
        final StoreKey key = new StoreKey( storeType, name );

        Response response;
        try
        {
            contentController.rescan( key );
            response = responseHelper.markDeprecated( Response.ok(), altPath )
                               .build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to rescan: %s. Reason: %s", key, e.getMessage() ), e );
            response = responseHelper.formatResponse( e, rb->responseHelper.markDeprecated( rb, altPath ) );
        }
        return response;
    }

    @ApiOperation(
            "Rescan all content in the specified repository to re-initialize metadata, capture missing index keys, etc." )
    @ApiResponse( code = 200,
                  message = "Rescan was started successfully. (NOTE: There currently is no way to determine when rescanning is complete.)" )
    @Path( "/rescan/{packageType}/{type: (hosted|group|remote)}/{name}" )
    @GET
    public Response rescan( @ApiParam( value = "The package type (eg. maven, npm, generic-http)", required = true )
                            @PathParam( "packageType" ) final String packageType,
                            @ApiParam( value = "The type of store / repository",
                                       allowableValues = "hosted,group,remote", required = true ) final @PathParam(
                                    "type" ) String type,
                            @ApiParam( "The name of the store / repository" ) @PathParam( "name" ) final String name )
    {
        final StoreKey key = new StoreKey( packageType, StoreType.get( type ), name );

        Response response;
        try
        {
            contentController.rescan( key );
            response = Response.ok().build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to rescan: %s. Reason: %s", key, e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @ApiOperation( "Rescan all content in all repositories to re-initialize metadata, capture missing index keys, etc." )
    @ApiResponse( code = 200, message = "Rescan was started successfully. (NOTE: There currently is no way to determine when rescanning is complete.)" )
    @Path( "/rescan/all" )
    @GET
    public Response rescanAll()
    {
        Response response;
        try
        {
            contentController.rescanAll();
            response = Response.ok()
                               .build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to rescan: ALL. Reason: %s", e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    /**
     * @deprecated use /content/all{path} instead
     * @param path
     * @return
     */
    @Deprecated
    @ApiOperation( "Delete the specified path globally (from any repository that contains it)." )
    @ApiResponse( code = 200, message = "Global deletion complete for path." )
    @Path( "/delete/all{path: (/.+)?}" )
    @DELETE
    public Response deleteAllViaGet( @ApiParam( "The path to delete globally" ) final @PathParam( "path" ) String path )
    {
        Response response;
        try
        {
            contentController.deleteAll( path );
            response = Response.ok()
                               .build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to delete: %s in: ALL. Reason: %s", e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @ApiOperation( "Delete the specified path globally (from any repository that contains it)." )
    @ApiResponse( code = 200, message = "Global deletion complete for path." )
    @Path( "/content/all{path: (/.+)?}" )
    @DELETE
    public Response deleteAll( @ApiParam( "The path to delete globally" ) final @PathParam( "path" ) String path )
    {
        Response response;
        try
        {
            contentController.deleteAll( path );
            response = Response.ok()
                               .build();
        }
        catch ( final IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to delete: %s in: ALL. Reason: %s", e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @Inject
    private IspnCacheController ispnCacheController;

    @ApiOperation( "Clean the specified Infinispan cache." )
    @ApiResponse( code = 200, message = "Clean complete." )
    @Path( "/infinispan/cache/{name}" )
    @DELETE
    public Response cleanInfinispanCache(
                    @ApiParam( "The name of cache to clean" ) @PathParam( "name" ) final String name )
    {
        Response response;
        try
        {
            ispnCacheController.clean( name );
            response = Response.ok().build();
        }
        catch ( IndyWorkflowException e )
        {
            logger.error( String.format( "Failed to clean: %s. Reason: %s", name, e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @ApiOperation( "Export the specified Infinispan cache." )
    @ApiResponse( code = 200, message = "Export complete." )
    @Produces( "application/json" )
    @Path( "/infinispan/cache/{name}{key: (/.+)?}" )
    @GET
    public Response exportInfinispanCache(
                    @ApiParam( "The name of cache to export" ) @PathParam( "name" ) final String name,
                    @ApiParam( "The cache key" ) @PathParam( "key" ) final String key
    )
    {
        Response response;
        try
        {
            String json = ispnCacheController.export( name, key );
            response = Response.ok( json ).build();
        }
        catch ( final Exception e )
        {
            logger.error( String.format( "Failed to export: %s. Reason: %s", name, e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

    @ApiOperation( "Get groups affected by specified repo." )
    @ApiResponse( code = 200, message = "Complete." )
    @Produces( "application/json" )
    @Path( "/store/affected/{key}" )
    @GET
    public Response affectedBy( @ApiParam( "The store key" ) @PathParam( "key" ) final String key )
    {
        Response response;
        try
        {
            Set<StoreKey> storeKeys = new HashSet<>();
            storeKeys.add( StoreKey.fromString( key ) );
            Set<Group> groups = storeDataManager.affectedBy( storeKeys );
            response = Response.ok( mapper.writeValueAsString( groups ) ).build();
        }
        catch ( final Exception e )
        {
            logger.error( String.format( "Failed to export: %s. Reason: %s", key, e.getMessage() ), e );
            response = responseHelper.formatResponse( e );
        }
        return response;
    }

}
