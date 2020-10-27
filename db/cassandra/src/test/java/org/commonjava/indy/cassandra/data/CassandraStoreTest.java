package org.commonjava.indy.cassandra.data;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.commonjava.indy.core.conf.IndyStoreManagerConfig;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.pkg.PackageTypeConstants;
import org.commonjava.indy.subsys.cassandra.CassandraClient;
import org.commonjava.indy.subsys.cassandra.config.CassandraConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CassandraStoreTest
{

    CassandraClient client;

    CassandraStoreQuery storeQuery;

    @Before
    public void start() throws Exception
    {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();

        CassandraConfig config = new CassandraConfig();
        config.setEnabled( true );
        config.setCassandraHost( "localhost" );
        config.setCassandraPort( 9142 );

        client = new CassandraClient( config );
        IndyStoreManagerConfig storeConfig = new IndyStoreManagerConfig( "noncontent", 1);

        storeQuery = new CassandraStoreQuery( client, storeConfig );

    }

    @After
    public void stop()
    {
        client.close();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void testQuery()
    {
        DtxArtifactStore store = createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );

        Set<DtxArtifactStore> storeSet = storeQuery.getAllArtifactStores();

        assertThat(storeSet.size(), equalTo( 1 ));

        storeQuery.removeArtifactStore( store.getPackageType(), StoreType.hosted, store.getName() );

        Set<DtxArtifactStore> storeSet2 = storeQuery.getAllArtifactStores();

        assertThat(storeSet2.size(), equalTo( 0 ));

    }

    @Test
    public void testIsEmpty()
    {

        assertThat( storeQuery.isEmpty(), equalTo( Boolean.FALSE ));

        createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );

        assertThat( storeQuery.isEmpty(), equalTo( Boolean.TRUE ));
    }

    @Test
    public void testGetStoreByPkgAndType()
    {

        createTestStore( PackageTypeConstants.PKG_TYPE_MAVEN, StoreType.hosted.name() );
        Set<DtxArtifactStore> artifactStoreSet =
                        storeQuery.getArtifactStoresByPkgAndType( PackageTypeConstants.PKG_TYPE_MAVEN,
                                                                  StoreType.hosted );
        assertThat(artifactStoreSet.size(), equalTo( 1 ));
    }

    private DtxArtifactStore createTestStore( final String packageType, final String storeType )
    {
        DtxArtifactStore store = new DtxArtifactStore();
        store.setPackageType( packageType );
        store.setStoreType( storeType );
        store.setName( "build-01" );
        store.setDescription( "test cassandra store" );
        store.setDisabled( true );

        Set<String> maskPatterns = new HashSet<>(  );

        store.setPathMaskPatterns( maskPatterns );
        storeQuery.createDtxArtifactStore( store );

        return store;
    }

}
