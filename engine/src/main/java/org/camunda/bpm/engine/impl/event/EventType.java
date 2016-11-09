/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.event;

/**
 * Defines the existing event types, on which the subscription can be done.
 *
 * Since the the event type for message and signal are historically lower case
 * the enum variant can't be used, so we have to reimplement an enum like class.
 * That is done so we can restrict the event types to only the defined ones.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public final class EventType {

  public static final EventType MESSAGE = new EventType("message");
  public static final EventType SIGNAL = new EventType("signal");
  public static final EventType COMPENSATE = new EventType("compensate");
  public static final EventType CONDITONAL = new EventType("conditional");

  private final String name;

  private EventType(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }
}
