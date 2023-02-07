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
package org.commonjava.indy.core.bind.jaxrs.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.commonjava.o11yphant.metrics.annotation.Measure;
import org.commonjava.o11yphant.metrics.api.Meter;
import org.commonjava.o11yphant.metrics.DefaultMetricsManager;
import org.commonjava.indy.subsys.metrics.conf.IndyMetricsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.commonjava.indy.IndyContentConstants.NANOS_PER_SEC;
import static org.commonjava.o11yphant.metrics.MetricsConstants.METER;
import static org.commonjava.o11yphant.metrics.util.NameUtils.getDefaultName;
import static org.commonjava.o11yphant.metrics.util.NameUtils.getName;

public class TransferStreamingOutput
    implements StreamingOutput
{

    private static final String TRANSFER_METRIC_NAME = "indy.transferred.content";

    private InputStream stream;

    private DefaultMetricsManager metricsManager;

    private IndyMetricsConfig metricsConfig;

    public TransferStreamingOutput( final InputStream stream, final DefaultMetricsManager metricsManager,
                                    final IndyMetricsConfig metricsConfig )
    {
        this.stream = stream;
        this.metricsManager = metricsManager;
        this.metricsConfig = metricsConfig;
    }

    @Override
    @Measure
    public void write( final OutputStream out )
        throws IOException, WebApplicationException
    {
        long start = System.nanoTime();
        try
        {
            CountingOutputStream cout = new CountingOutputStream( out );
            IOUtils.copy( stream, cout );

            Logger logger = LoggerFactory.getLogger( getClass() );
            logger.trace( "Wrote: {} bytes", cout.getByteCount() );

            String name = getName( metricsConfig.getNodePrefix(), TRANSFER_METRIC_NAME,
                                   getDefaultName( TransferStreamingOutput.class, "write" ), METER );

            long end = System.nanoTime();
            double elapsed = (end-start)/NANOS_PER_SEC;

            Meter meter = metricsManager.getMeter( name );
            meter.mark( Math.round( cout.getByteCount() / elapsed ) );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }
    }

}
