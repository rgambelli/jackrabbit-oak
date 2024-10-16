/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document.mongo;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ClusterType;

import org.apache.jackrabbit.oak.plugins.document.util.MongoConnection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MongoConnectionTest {

    @Test
    public void hasWriteConcern() throws Exception {
        assertTrue(MongoConnection.hasMongoDbDefaultWriteConcern("mongodb://localhost:27017/foo"));
        assertFalse(MongoConnection.hasMongoDbDefaultWriteConcern("mongodb://localhost:27017/foo?w=1"));
    }

    @Test
    public void hasReadConcern() throws Exception {
        assertFalse(MongoConnection.hasReadConcern("mongodb://localhost:27017/foo"));
        assertTrue(MongoConnection.hasReadConcern("mongodb://localhost:27017/foo?readconcernlevel=majority"));
    }

    @Test
    public void sufficientWriteConcern() throws Exception {
        sufficientWriteConcernReplicaSet(WriteConcern.ACKNOWLEDGED, false);
        sufficientWriteConcernReplicaSet(WriteConcern.JOURNALED, false);
        sufficientWriteConcernReplicaSet(WriteConcern.MAJORITY, true);
        sufficientWriteConcernReplicaSet(WriteConcern.W2, true);
        sufficientWriteConcernReplicaSet(WriteConcern.UNACKNOWLEDGED, false);

        sufficientWriteConcernSingleNode(WriteConcern.ACKNOWLEDGED, true);
        sufficientWriteConcernSingleNode(WriteConcern.JOURNALED, true);
        sufficientWriteConcernSingleNode(WriteConcern.MAJORITY, true);
        sufficientWriteConcernReplicaSet(WriteConcern.W2, true);
        sufficientWriteConcernSingleNode(WriteConcern.UNACKNOWLEDGED, false);
    }

    @Test
    public void sufficientReadConcern() throws Exception {
        sufficientReadConcernReplicaSet(ReadConcern.DEFAULT, false);
        sufficientReadConcernReplicaSet(ReadConcern.LOCAL, false);
        sufficientReadConcernReplicaSet(ReadConcern.MAJORITY, true);

        sufficientReadConcernSingleNode(ReadConcern.DEFAULT, true);
        sufficientReadConcernSingleNode(ReadConcern.LOCAL, true);
        sufficientReadConcernSingleNode(ReadConcern.MAJORITY, true);
    }

    private void sufficientWriteConcernReplicaSet(WriteConcern w,
                                                  boolean sufficient) {
        sufficientWriteConcern(w, true, sufficient);
    }

    private void sufficientWriteConcernSingleNode(WriteConcern w,
                                                      boolean sufficient) {
        sufficientWriteConcern(w, false, sufficient);
    }

    private void sufficientWriteConcern(WriteConcern w,
                                        boolean replicaSet,
                                        boolean sufficient) {
        MongoClient mongo = mockMongoClient(replicaSet);
        assertEquals(sufficient, MongoConnection.isSufficientWriteConcern(mongo, w));
    }

    private void sufficientReadConcernReplicaSet(ReadConcern r,
                                                 boolean sufficient) {
        sufficientReadConcern(r, true, sufficient);
    }

    private void sufficientReadConcernSingleNode(ReadConcern r,
                                                 boolean sufficient) {
        sufficientReadConcern(r, false, sufficient);
    }
    private void sufficientReadConcern(ReadConcern r,
                                       boolean replicaSet,
                                       boolean sufficient) {
        MongoClient mongo = mockMongoClient(replicaSet);
        assertEquals(sufficient, MongoConnection.isSufficientReadConcern(mongo, r));
    }

    private MongoClient mockMongoClient(boolean replicaSet) {
        ClusterDescription description = mock(ClusterDescription.class);
        if (replicaSet) {
            when(description.getType()).thenReturn(ClusterType.REPLICA_SET);
        } else {
            when(description.getType()).thenReturn(ClusterType.STANDALONE);
        }
        MongoClient client = mock(MongoClient.class);
        when(client.getClusterDescription()).thenReturn(description);
        
        return client;
    }
}
