/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.test.integration;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.ClusterAgent;
import net.kuujo.vertigo.cluster.ClusterManager;
import net.kuujo.vertigo.cluster.ClusterScope;
import net.kuujo.vertigo.cluster.impl.RemoteClusterManager;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.impl.DefaultNetworkConfig;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterManagerTest extends TestVerticle {

  @Test
  public void testLocalDeploy() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = new DefaultNetworkConfig("test-local-deploy");
        network.getClusterConfig().setAddress("test");
        network.getClusterConfig().setScope(ClusterScope.CLUSTER);
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker1", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream1", "worker", "stream1");
        network.createConnection("feeder", "stream2", "worker", "stream2");

        ClusterManager cluster = new RemoteClusterManager("test", vertx, container);
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testLocalShutdown() {
    net.kuujo.xync.util.Cluster.initialize();
    container.deployWorkerVerticle(ClusterAgent.class.getName(), new JsonObject().putString("cluster", "test"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        NetworkConfig network = new DefaultNetworkConfig("test-local-shutdown");
        network.getClusterConfig().setAddress("test");
        network.getClusterConfig().setScope(ClusterScope.CLUSTER);
        network.addVerticle("feeder", TestFeeder.class.getName());
        network.addVerticle("worker1", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream1", "worker", "stream1");
        network.addVerticle("worker2", TestWorker.class.getName(), 2);
        network.createConnection("feeder", "stream2", "worker", "stream2");

        final ClusterManager cluster = new RemoteClusterManager("test", vertx, container);
        cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
          @Override
          public void handle(AsyncResult<ActiveNetwork> result) {
            assertTrue(result.succeeded());
            vertx.setTimer(2000, new Handler<Long>() {
              @Override
              public void handle(Long timerID) {
                cluster.undeployNetwork("test-local-shutdown", new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    assertTrue(result.succeeded());
                    testComplete();
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  public static class TestFeeder extends ComponentVerticle {
  }

  public static class TestWorker extends ComponentVerticle {
  }

}