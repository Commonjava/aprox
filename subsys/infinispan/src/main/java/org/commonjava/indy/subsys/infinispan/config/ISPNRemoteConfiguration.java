/**
 * Copyright (C) 2011-2023 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.subsys.infinispan.config;

import org.commonjava.indy.conf.IndyConfigInfo;
import org.commonjava.propulsor.config.annotation.ConfigName;
import org.commonjava.propulsor.config.annotation.SectionName;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.InputStream;

@SectionName( "infinispan-remote" )
@ApplicationScoped
public class ISPNRemoteConfiguration
        implements IndyConfigInfo
{
    private static final String DEFAULT_REMOTE_SERVER = "localhost";

    private static final Boolean DEFAULT_ENABLED = Boolean.FALSE;

    private Boolean enabled;

    private String hotrodClientConfigPath;

    public ISPNRemoteConfiguration()
    {
    }

    public Boolean isEnabled()
    {
        return enabled == null ? DEFAULT_ENABLED : enabled;
    }

    @ConfigName( "enabled" )
    public void setEnabled( Boolean enabled )
    {
        this.enabled = enabled;
    }

    public String getHotrodClientConfigPath()
    {
        return hotrodClientConfigPath;
    }

    @ConfigName( "hotrod.client.config" )
    public void setHotrodClientConfigPath( String hotrodClientConfigPath )
    {
        this.hotrodClientConfigPath = hotrodClientConfigPath;
    }

    @Override
    public String getDefaultConfigFileName()
    {
        return new File( IndyConfigInfo.CONF_INCLUDES_DIR, "infinispan-remote.conf" ).getPath();
    }

    @Override
    public InputStream getDefaultConfig()
    {
        return Thread.currentThread()
                     .getContextClassLoader()
                     .getResourceAsStream( "default-infinispan-remote.conf" );
    }
}
