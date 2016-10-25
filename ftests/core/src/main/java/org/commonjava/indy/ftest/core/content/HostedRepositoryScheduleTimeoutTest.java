/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.indy.ftest.core.content;

import org.commonjava.indy.client.core.helper.PathInfo;
import org.commonjava.indy.ftest.core.AbstractContentManagementTest;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.test.fixture.core.CoreServerFixture;
import org.junit.Test;

import java.io.IOException;

import static org.commonjava.indy.model.core.StoreType.hosted;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class HostedRepositoryScheduleTimeoutTest
        extends AbstractContentManagementTest
{

    @Test
    public void repoTimeout()
            throws Exception
    {

        final int REPO_TIMEOUT_SECONDS = 6;
        final int TIMEOUT_WAITING_MILLISECONDS = 7000;

        final String path = "/path/to/foo.class";

        final String hostedName = "test";

        final HostedRepository repo = new HostedRepository( hostedName );
        repo.setRepoTimeoutSeconds( REPO_TIMEOUT_SECONDS );

        final HostedRepository result = client.stores().create( repo, name.getMethodName(), HostedRepository.class );

        assertNotNull( result );

        PathInfo pomResult = client.content().getInfo( hosted, hostedName, path );
        client.content().get( hosted, hostedName, path );

        assertNotNull( pomResult );
        assertThat( pomResult.exists(), equalTo( true ) );
        assertThat( client.stores().exists( StoreType.hosted, hostedName ), equalTo( true ) );

        // wait for 7s
        Thread.sleep( TIMEOUT_WAITING_MILLISECONDS );

        assertThat( pomResult.exists(), equalTo( false ) );
        assertThat( client.stores().exists( StoreType.hosted, hostedName ), equalTo( false ) );
    }

    @Override
    protected void initTestConfig( CoreServerFixture fixture )
            throws IOException
    {
        writeConfigFile( "main.conf", readTestResource( "default-test-main.conf" ) );
    }
}
