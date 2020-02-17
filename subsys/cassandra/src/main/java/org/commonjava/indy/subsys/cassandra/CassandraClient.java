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
package org.commonjava.indy.subsys.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import org.commonjava.indy.subsys.cassandra.config.CassandraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@ApplicationScoped
public class CassandraClient
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private CassandraConfig config;

    private Session session;

    private Cluster cluster;

    public CassandraClient()
    {
    }

    public CassandraClient( CassandraConfig config )
    {
        this.config = config;
        init();
    }

    @PostConstruct
    private void init()
    {
        if ( !config.isEnabled() )
        {
            logger.info( "Cassandra client not enabled" );
            return;
        }
        try
        {
            String host = config.getCassandraHost();
            int port = config.getCassandraPort();
            SocketOptions socketOptions = new SocketOptions();
            socketOptions.setConnectTimeoutMillis( 30000 );
            socketOptions.setReadTimeoutMillis( 30000 );
            Cluster.Builder builder = Cluster.builder()
                                             .withoutJMXReporting()
                                             .addContactPoint( host )
                                             .withPort( port )
                                             .withSocketOptions( socketOptions );
            String username = config.getCassandraUser();
            String password = config.getCassandraPass();
            if ( isNotBlank( username ) && isNotBlank( password ) )
            {
                logger.info( "Build with credentials, user: {}, pass: ****", username );
                builder.withCredentials( username, password );
            }

            cluster = builder.build();

            logger.info( "Connecting to Cassandra, host:{}, port:{}, user:{}", host, port, username );
            session = cluster.connect();
        }
        catch ( Exception e )
        {
            logger.error( "Connecting to Cassandra failed", e );
        }
    }

    public Session getSession()
    {
        return session;
    }

    private volatile boolean closed;

    public void close()
    {
        if ( !closed && cluster != null && session != null )
        {
            logger.info( "Close cassandra client" );
            session.close();
            cluster.close();
            session = null;
            cluster = null;
            closed = true;
        }
    }
}
