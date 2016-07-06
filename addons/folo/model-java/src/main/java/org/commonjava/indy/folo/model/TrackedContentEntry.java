/**
 * Copyright (C) 2011 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.indy.folo.model;

import org.commonjava.indy.model.core.StoreKey;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Indexed
public class TrackedContentEntry
        implements Comparable<TrackedContentEntry>
{

    @IndexedEmbedded
    private TrackingKey trackingKey;

    @Field
    private StoreKey storeKey;

    @Field
    private String path;

    @Field
    private String originUrl;

    @Field
    private StoreEffect effect;

    @Field
    private String md5;

    @Field
    private String sha256;

    @Field
    private String sha1;

    @Field
    private long index = System.currentTimeMillis();

    public TrackedContentEntry()
    {
    }

    public TrackedContentEntry( final TrackingKey trackingKey, final StoreKey storeKey, final String originUrl,
                                final String path, final StoreEffect effect, final String md5, final String sha1,
                                final String sha256 )
    {
        this.trackingKey = trackingKey;
        this.storeKey = storeKey;
        this.path = path;
        this.originUrl = originUrl;
        this.effect = effect;
        this.md5=md5;
        this.sha1=sha1;
        this.sha256=sha256;
    }

    public String getOriginUrl()
    {
        return originUrl;
    }

    public String getMd5()
    {
        return md5;
    }

    public String getSha256()
    {
        return sha256;
    }

    public String getSha1()
    {
        return sha1;
    }

    public StoreKey getStoreKey()
    {
        return storeKey;
    }

    public String getPath()
    {
        return path;
    }

    public TrackingKey getTrackingKey()
    {
        return trackingKey;
    }

    public StoreEffect getEffect()
    {
        return effect;
    }

    public long getIndex()
    {
        return index;
    }

    @Override
    public int compareTo( final TrackedContentEntry other )
    {
        int comp = storeKey.compareTo( other.getStoreKey() );
        if ( comp == 0 )
        {
            comp = path.compareTo( other.getPath() );
        }

        return comp;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( path == null ) ? 0 : path.hashCode() );
        result = prime * result + ( ( storeKey == null ) ? 0 : storeKey.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final TrackedContentEntry other = (TrackedContentEntry) obj;
        if ( path == null )
        {
            if ( other.path != null )
            {
                return false;
            }
        }
        else if ( !path.equals( other.path ) )
        {
            return false;
        }
        if ( storeKey == null )
        {
            if ( other.storeKey != null )
            {
                return false;
            }
        }
        else if ( !storeKey.equals( other.storeKey ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return String.format(
                "TrackedContentEntry [\n  trackingKey=%s\n  storeKey=%s\n  path=%s\n  originUrl=%s\n effect=%s\n  md5=%s\n  sha1=%s\n  sha256=%s\n]",
                trackingKey, storeKey, path, originUrl, effect, md5, sha1, sha256 );
    }

}
