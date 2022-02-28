package org.commonjava.indy.folo.bind.jaxrs;

import io.swagger.annotations.*;
import org.commonjava.indy.bind.jaxrs.IndyDeployment;
import org.commonjava.indy.bind.jaxrs.IndyResources;
import org.commonjava.indy.bind.jaxrs.util.REST;
import org.commonjava.indy.core.bind.jaxrs.ContentAccessHandler;
import org.commonjava.indy.core.bind.jaxrs.util.RequestUtils;
import org.commonjava.indy.folo.model.TrackingKey;
import org.commonjava.indy.model.core.AccessChannel;
import org.commonjava.indy.util.RequestContextHelper;
import org.commonjava.maven.galley.event.EventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import static org.commonjava.indy.IndyContentConstants.CHECK_CACHE_ONLY;
import static org.commonjava.indy.folo.ctl.FoloConstants.ACCESS_CHANNEL;
import static org.commonjava.indy.folo.ctl.FoloConstants.TRACKING_KEY;
import static org.commonjava.indy.pkg.PackageTypeConstants.PKG_TYPE_GENERIC_HTTP;
import static org.commonjava.indy.util.RequestContextHelper.CONTENT_TRACKING_ID;
import static org.commonjava.maven.galley.spi.cache.CacheProvider.STORE_HTTP_HEADERS;

@Api( value = "FOLO Tracked Content Access and Storage. Tracks retrieval and management of file/artifact content." )
@Path( "/api/folo/track/{id}/generic-http/{type: (hosted|group|remote)}/{name}" )
@REST
public class FoloGenericContentAccessResource
        implements IndyResources
{

    private static final String BASE_PATH = IndyDeployment.API_PREFIX + "/folo/track";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private ContentAccessHandler handler;

    public FoloGenericContentAccessResource()
    {
    }

    public FoloGenericContentAccessResource( final ContentAccessHandler handler )
    {
        this.handler = handler;
    }

    @ApiOperation( "Store and track file/artifact content under the given artifact store (type/name) and path." )
    @ApiResponses( { @ApiResponse( code = 201, message = "Content was stored successfully" ), @ApiResponse( code = 400,
            message = "No appropriate storage location was found in the specified store (this store, or a member if a group is specified)." ) } )
    @PUT
    @Path( "/{path: (.*)}" )
    public Response doCreate(@ApiParam( "User-assigned tracking session key" ) @PathParam( "id" ) final String id,
                             @ApiParam( allowableValues = "hosted,group,remote", required = true ) @PathParam( "type" )
                             final String type, @PathParam( "name" ) final String name,
                             @PathParam( "path" ) final String path, @Context final HttpServletRequest request,
                             @Context final UriInfo uriInfo )
    {
        final TrackingKey tk = new TrackingKey( id );

        EventMetadata metadata =
                new EventMetadata().set( TRACKING_KEY, tk ).set( ACCESS_CHANNEL, AccessChannel.NATIVE ).set(
                        STORE_HTTP_HEADERS, RequestUtils.extractRequestHeadersToMap( request ) );

        Class cls = FoloGenericContentAccessResource.class;
        return handler.doCreate( PKG_TYPE_GENERIC_HTTP, type, name, path, request, metadata, () -> uriInfo.getBaseUriBuilder()
                .path( cls )
                .path( path )
                .build( id, PKG_TYPE_GENERIC_HTTP, type, name ) );
    }

    @ApiOperation( "Store and track file/artifact content under the given artifact store (type/name) and path." )
    @ApiResponses( { @ApiResponse( code=404, message = "Content is not available" ), @ApiResponse( code = 200,
            message = "Header metadata for content (or rendered listing when path ends with '/index.html' or '/'" ), } )
    @HEAD
    @Path( "/{path: (.*)}" )
    public Response doHead( @ApiParam( "User-assigned tracking session key" ) @PathParam( "id" ) final String id,
                            @ApiParam( allowableValues = "hosted,group,remote", required = true ) @PathParam( "type" )
                            final String type, @PathParam( "name" ) final String name,
                            @PathParam( "path" ) final String path,
                            @QueryParam( CHECK_CACHE_ONLY ) final Boolean cacheOnly,
                            @Context final HttpServletRequest request, @Context final UriInfo uriInfo )
    {
        final TrackingKey tk = new TrackingKey( id );

        final String baseUri = uriInfo.getBaseUriBuilder().path( BASE_PATH ).path( id ).build().toString();

        EventMetadata metadata =
                new EventMetadata().set( TRACKING_KEY, tk ).set( ACCESS_CHANNEL, AccessChannel.NATIVE );

        RequestContextHelper.setContext( CONTENT_TRACKING_ID, id );

        return handler.doHead( PKG_TYPE_GENERIC_HTTP, type, name, path, cacheOnly, baseUri, request, metadata );
    }

    @ApiOperation( "Retrieve and track file/artifact content under the given artifact store (type/name) and path." )
    @ApiResponses( { @ApiResponse( code=404, message = "Content is not available" ), @ApiResponse( code = 200, response = String.class,
            message = "Rendered content listing (when path ends with '/index.html' or '/')" ),
            @ApiResponse( code = 200, response = StreamingOutput.class, message = "Content stream" ), } )
    @GET
    @Path( "/{path: (.*)}" )
    public Response doGet( @ApiParam( "User-assigned tracking session key" ) @PathParam( "id" ) final String id,
                           @ApiParam( allowableValues = "hosted,group,remote", required = true ) @PathParam( "type" )
                           final String type, @PathParam( "name" ) final String name,
                           @PathParam( "path" ) final String path, @Context final HttpServletRequest request,
                           @Context final UriInfo uriInfo )
    {
        final TrackingKey tk = new TrackingKey( id );
        final String baseUri = uriInfo.getBaseUriBuilder().path( BASE_PATH ).path( id ).build().toString();

        EventMetadata metadata =
                new EventMetadata().set( TRACKING_KEY, tk ).set( ACCESS_CHANNEL, AccessChannel.NATIVE );

        RequestContextHelper.setContext( CONTENT_TRACKING_ID, id );

        return handler.doGet( PKG_TYPE_GENERIC_HTTP, type, name, path, baseUri, request, metadata );
    }

}
