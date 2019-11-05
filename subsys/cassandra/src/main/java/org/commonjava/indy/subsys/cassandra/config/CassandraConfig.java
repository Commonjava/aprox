/**
 * Copyright (C) 2011-2019 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.subsys.cassandra.config;

import org.commonjava.indy.conf.IndyConfigInfo;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;

@SectionName( "cassandra" )
@ApplicationScoped
public class CassandraConfig
                implements IndyConfigInfo
{
    private String cassandraHost;

    private Integer cassandraPort;

    private String cassandraUser;

    private String cassandraPass;

    public CassandraConfig()
    {
    }

    private static final String DEFAULT_CASSANDRA_HOST = "localhost";

    private static final int DEFAULT_CASSANDRA_PORT = 9042;

    @ConfigName( "cassandra.host" )
    public void setCassandraHost( String host )
    {
        cassandraHost = host;
    }

    @ConfigName( "cassandra.port" )
    public void setCassandraPort( Integer port )
    {
        cassandraPort = port;
    }

    @ConfigName( "cassandra.user" )
    public void setCassandraUser( String cassandraUser )
    {
        this.cassandraUser = cassandraUser;
    }

    @ConfigName( "cassandra.pass" )
    public void setCassandraPass( String cassandraPass )
    {
        this.cassandraPass = cassandraPass;
    }

    public String getCassandraHost()
    {
        return cassandraHost == null ? DEFAULT_CASSANDRA_HOST : cassandraHost;
    }

    public Integer getCassandraPort()
    {
        return cassandraPort == null ? DEFAULT_CASSANDRA_PORT : cassandraPort;
    }

    public String getCassandraUser()
    {
        return cassandraUser;
    }

    public String getCassandraPass()
    {
        return cassandraPass;
    }

    @Override
    public String getDefaultConfigFileName()
    {
        return "default-cassandra.conf";
    }

    @Override
    public InputStream getDefaultConfig()
    {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream( "default-cassandra.conf" );
    }

}
