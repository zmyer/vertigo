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
package net.kuujo.vertigo.hooks;

import net.kuujo.vertigo.input.InputCollector;

/**
 * An input hook.
 *
 * @author Jordan Halterman
 */
public interface InputHook extends Hook<InputCollector> {

  /**
   * Called when the component receives an input message.
   *
   * @param id
   *   The unique message identifier.
   */
  public void received(String id);

  /**
   * Called when the component acks a received message.
   *
   * @param id
   *   The unique message identifier.
   */
  public void ack(String id);

  /**
   * Called when the component fails a received message.
   *
   * @param id
   *   The unique message identifier.
   */
  public void fail(String id);

}
