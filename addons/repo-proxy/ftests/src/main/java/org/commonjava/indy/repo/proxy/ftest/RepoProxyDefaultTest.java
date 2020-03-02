/**
 * Copyright (C) 2013 Red Hat, Inc.
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
package org.commonjava.indy.repo.proxy.ftest;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.ftest.core.AbstractContentManagementTest;
import org.commonjava.indy.model.core.HostedRepository;
import org.commonjava.indy.model.core.RemoteRepository;
import org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor;
import org.commonjava.indy.test.fixture.core.CoreServerFixture;
import org.commonjava.test.http.expect.ExpectationServer;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Check if the repo proxy addon can proxy a hosted repo to a remote repo
 * <br/>
 * GIVEN:
 * <ul>
 *     <li>Hosted and Remote Repo</li>
 *     <li>Hosted does not contain pathA but remote does</li>
 *     <li>Hosted contains pathB but remote does not</li>
 *     <li>Configured the repo-proxy disabled</li>
 *     <li>Configured proxy rule with mapping hosted proxy to remote</li>
 * </ul>
 * <br/>
 * WHEN:
 * <ul>
 *     <li>Request pathA and pathB through hosted</li>
 * </ul>
 * <br/>
 * THEN:
 * <ul>
 *     <li>The content of pathA can be returned correctly from hosted, but pathB can not</li>
 * </ul>
 */
public class RepoProxyDefaultTest
        extends AbstractContentManagementTest
{
    private static final String REPO_NAME = "pnc-builds";

    private HostedRepository hosted;

    private RemoteRepository remote;

    private static final String PATH1 = "foo/bar/1.0/foo-bar-1.0.txt";

    private static final String PATH2 = "foo/bar/2.0/foo-bar-2.0.txt";

    private static final String CONTENT1 = "This is content 1";

    private static final String CONTENT2 = "This is content 2";

    @Before
    public void setupRepos()
            throws Exception
    {

        server.expect( server.formatUrl( REPO_NAME, PATH1 ), 200, new ByteArrayInputStream( CONTENT1.getBytes() ) );

        remote = client.stores()
                       .create( new RemoteRepository( MavenPackageTypeDescriptor.MAVEN_PKG_KEY, REPO_NAME,
                                                      server.formatUrl( REPO_NAME ) ), "remote pnc-builds",
                                RemoteRepository.class );

        hosted = client.stores()
                       .create( new HostedRepository( MavenPackageTypeDescriptor.MAVEN_PKG_KEY, REPO_NAME ),
                                "hosted pnc-builds", HostedRepository.class );
        client.content().store( hosted.getKey(), PATH2, new ByteArrayInputStream( CONTENT2.getBytes() ) );
    }

    @Test
    public void run()
            throws Exception
    {
        try (InputStream result = client.content().get( remote.getKey(), PATH1 ))
        {
            assertThat( result, notNullValue() );
            final String content = IOUtils.toString( result );
            assertThat( content, equalTo( CONTENT1 ) );
        }

        try (InputStream result = client.content().get( hosted.getKey(), PATH1 ))
        {
            assertThat( result, notNullValue() );
            final String content = IOUtils.toString( result );
            assertThat( content, equalTo( CONTENT1 ) );
        }

        InputStream result = client.content().get( remote.getKey(), PATH2 );
        assertThat( result, nullValue() );

        result = client.content().get( hosted.getKey(), PATH2 );
        assertThat( result, nullValue() );
    }

    @Override
    protected void initTestConfig( CoreServerFixture fixture )
            throws IOException
    {
        writeConfigFile( "conf.d/repo-proxy.conf",
                         "[repo-proxy]\nenabled=true\nproxy.maven.hosted.pnc-builds=maven:remote:pnc-builds" );
    }

}
