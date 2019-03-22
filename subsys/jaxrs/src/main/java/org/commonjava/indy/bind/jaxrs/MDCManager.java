/**
 * Copyright (C) 2011-2018 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.bind.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.commonjava.indy.conf.IndyConfiguration;
import org.commonjava.indy.conf.IndyLoggingConfig;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.CLIENT_ADDR;
import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.COMPONENT_ID;
import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.ENVIRONMENT;
import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.EXTERNAL_ID;
import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.INTERNAL_ID;
import static org.commonjava.indy.bind.jaxrs.RequestContextConstants.PREFERRED_ID;

@ApplicationScoped
public class MDCManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private IndyConfiguration config;

    @Inject
    private IndyLoggingConfig loggingConfig;

    @Inject
    private IndyObjectMapper objectMapper;

    public MDCManager() {}

    public void clear()
    {
        MDC.clear();
    }

    private List<String> mdcHeadersList = new ArrayList();

    @PostConstruct
    public void setUp()
    {
        mdcHeadersList.add( COMPONENT_ID ); // default

        String mdcHeaders = config.getMdcHeaders();
        if ( isNotBlank( mdcHeaders ) )
        {
            String[] headers = mdcHeaders.split( "," );
            for ( String s : headers )
            {
                if ( isNotBlank( s ) && !mdcHeadersList.contains( s ) )
                {
                    mdcHeadersList.add( s.trim() );
                }
            }
        }
    }

    public void putExternalID( String externalID )
    {
        if ( externalID == null )
        {
            logger.debug( "No externalId" );
            return;
        }
        String internalID = UUID.randomUUID().toString();
        String preferredID = externalID != null ? externalID : internalID;
        putRequestIDs( internalID, externalID, preferredID );
    }

    public void putRequestIDs( String internalID, String externalID, String preferredID )
    {
        MDC.put( INTERNAL_ID, internalID );
        MDC.put( EXTERNAL_ID, externalID );
        MDC.put( PREFERRED_ID, preferredID );
    }

    public void putUserIP( String userIp )
    {
        MDC.put( CLIENT_ADDR, userIp );
    }

    public void putExtraHeaders( HttpServletRequest request )
    {
        mdcHeadersList.forEach( ( header ) -> MDC.put( header, request.getHeader( header ) ) );
    }

    public void putExtraHeaders( HttpRequest httpRequest )
    {
        mdcHeadersList.forEach( ( header ) -> {
            Header h = httpRequest.getFirstHeader( header );
            if ( h != null )
            {
                MDC.put( header, h.getValue() );
            }
        } );
    }

    public void putEnvironment()
    {
        try
        {
            MDC.put( ENVIRONMENT, objectMapper.writeValueAsString( loggingConfig.getEnvars() ) );
        }
        catch ( JsonProcessingException e )
        {
            MDC.put( ENVIRONMENT, "{error: \"Envars could not be processed by Jackson.\"}");
            logger.error( String.format( "Failed to create environment mdc. Reason: %s", e.getMessage() ), e );
        }
    }
}
