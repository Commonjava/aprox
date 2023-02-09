/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.ftest.core.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.commonjava.indy.model.core.HostedRepository;
import org.junit.Ignore;
import org.junit.Test;

@Ignore( " This test can be fully covered by AddAndDeleteHostedRepoTest" )
@Deprecated
public class AddAndRetrieveHostedRepoTest
        extends AbstractStoreManagementTest
{

    @Test
    public void addMinimalHostedRepositoryAndRetrieveIt()
            throws Exception
    {
        final HostedRepository repo = new HostedRepository( newName() );
        final HostedRepository result = client.stores().create( repo, name.getMethodName(), HostedRepository.class );

        assertThat( result.getName(), equalTo( repo.getName() ) );
        assertThat( result.equals( repo ), equalTo( true ) );
    }

}
