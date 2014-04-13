/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.input;

import java.util.Collection;

import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.streams.Closeable;
import net.kuujo.vertigo.streams.Openable;

/**
 * An input collector.<p>
 * 
 * The input collector is the primary interface for receiving input within a component
 * instance. Input collectors are simple wrappers around {@link InputPort} instances.
 * 
 * @author Jordan Halterman
 */
public interface InputCollector extends Openable<InputCollector>, Closeable<InputCollector> {

  /**
   * Returns the component input context.
   *
   * @return The input context.
   */
  InputContext context();

  /**
   * Returns a collection of input ports.
   *
   * @return A collection of input ports.
   */
  Collection<InputPort> ports();

  /**
   * Returns an input port. The port will be automatically created if it doesn't
   * already exist.
   *
   * @param name The name of the port to get.
   * @return The input port.
   */
  InputPort port(String name);

}
