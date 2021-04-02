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
package org.commonjava.indy.core.ctl;

import org.commonjava.indy.IndyWorkflowException;
import org.commonjava.indy.core.change.StoreEnablementManager;
import org.commonjava.indy.core.expire.Expiration;
import org.commonjava.indy.core.expire.ExpirationSet;
import org.commonjava.indy.core.expire.ScheduleManager;
import org.commonjava.indy.core.expire.ScheduleManagerUtils;
import org.commonjava.indy.core.expire.ScheduleValueDTO;
import org.commonjava.indy.data.IndyDataException;
import org.commonjava.indy.data.StoreDataManager;
import org.commonjava.indy.model.core.ArtifactStore;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.io.IndyObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletInputStream;

@ApplicationScoped
public class SchedulerController
{
    @Inject
    private ScheduleManager scheduleManager;

    @Inject
    private StoreDataManager storeDataManager;

    @Inject
    private IndyObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    protected SchedulerController()
    {
    }

    public SchedulerController( ScheduleManager scheduleManager, StoreDataManager storeDataManager )
    {
        this.scheduleManager = scheduleManager;
        this.storeDataManager = storeDataManager;
    }

    public Expiration getStoreDisableTimeout( StoreKey storeKey )
            throws IndyWorkflowException
    {
        try
        {
            Expiration expiration = scheduleManager.findSingleExpiration(
                    storeKey, StoreEnablementManager.DISABLE_TIMEOUT );

            if ( expiration == null )
            {
                ArtifactStore store = storeDataManager.getArtifactStore( storeKey );
                if ( store != null && store.isDisabled() )
                {
                    expiration = indefiniteDisable( store );
                }
            }

            return expiration;
        }
        catch ( IndyDataException e )
        {
            throw new IndyWorkflowException( "Failed to load store: %s to check for indefinite disable. Reason: %s", e, storeKey, e.getMessage() );
        }
    }

    public ExpirationSet getDisabledStores()
            throws IndyWorkflowException
    {
        try
        {

            // This key matcher will compare with the cache key group to see if the group ends with the "Disable-Timeout"(jobtype)
            ExpirationSet expirations = scheduleManager.findMatchingExpirations( StoreEnablementManager.DISABLE_TIMEOUT );

            // TODO: This seems REALLY inefficient...
            storeDataManager.getAllArtifactStores().forEach( (store)->{
                if ( store.isDisabled() )
                {
                    expirations.getItems().add( indefiniteDisable( store ) );
                }
            });

            return expirations;
        }
        catch ( IndyDataException e )
        {
            throw new IndyWorkflowException( "Failed to load stores to check for indefinite disable. Reason: %s", e, e.getMessage() );
        }
    }

    private Expiration indefiniteDisable( ArtifactStore store )
    {
        return new Expiration( ScheduleManagerUtils.groupName( store.getKey(), StoreEnablementManager.DISABLE_TIMEOUT ), StoreEnablementManager.DISABLE_TIMEOUT );
    }

    public String exportScheduler() throws Exception
    {
        return scheduleManager.exportScheduler();
    }

    public void importScheduler( ServletInputStream inputStream ) throws Exception
    {
        ScheduleValueDTO dto = objectMapper.readValue( inputStream, ScheduleValueDTO.class );
        logger.info( "Import schedulers, size: {}", dto.getItems().size() );
        scheduleManager.importScheduler( dto.getItems() );
    }
}
