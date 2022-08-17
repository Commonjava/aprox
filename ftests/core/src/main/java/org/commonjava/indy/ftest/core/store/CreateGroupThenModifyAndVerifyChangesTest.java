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
package org.commonjava.indy.ftest.core.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreType;
import org.junit.Test;

public class CreateGroupThenModifyAndVerifyChangesTest
    extends AbstractStoreManagementTest
{

    @Test
    public void addAndModifyGroupThenRetrieveIt()
        throws Exception
    {
        final Group repo = new Group( newName() );
        client.stores()
              .create( repo, name.getMethodName(), Group.class );

        repo.setDescription( "Testing" );

        assertThat( client.stores()
                          .update( repo, name.getMethodName() ), equalTo( true ) );

        final Group result = client.stores()
                                   .load( StoreType.group, repo.getName(), Group.class );

        assertThat( result.getName(), equalTo( repo.getName() ) );
        assertThat( result.equals( repo ), equalTo( true ) );
    }
}
