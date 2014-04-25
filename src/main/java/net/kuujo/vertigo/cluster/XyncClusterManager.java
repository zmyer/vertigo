/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.annotations.XyncType;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Xync cluster manager implementation.<p>
 *
 * The Xync cluster manager is backed by {@link XyncCluster} which relies
 * upon the Xync event bus API for fault-tolerant deployments and Hazelcast
 * data structure access. This means the Xync cluster manager depends upon
 * the Xync platform manager or a similar event bus API for operation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@XyncType
public class XyncClusterManager extends AbstractClusterManager {

  @Factory
  public static XyncClusterManager factory(Vertx vertx, Container container) {
    return new XyncClusterManager(vertx, container);
  }

  public XyncClusterManager(Verticle verticle) {
    this(verticle.getVertx(), verticle.getContainer());
  }

  public XyncClusterManager(Vertx vertx, Container container) {
    super(vertx, container, new XyncCluster(vertx, container));
  }

  @Override
  public ClusterScope scope() {
    return ClusterScope.XYNC;
  }

}
