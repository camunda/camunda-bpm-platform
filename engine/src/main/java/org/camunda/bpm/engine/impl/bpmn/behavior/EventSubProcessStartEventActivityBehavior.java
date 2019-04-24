/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;


/**
 * <p>Specialization of the Start Event for Event Sub-Processes.</p>
 *
 * <p>The start event's behavior is realized by the start behavior of the event subprocess it is embedded in.
 * The start behavior of the event subprocess must be either either {@link ActivityStartBehavior#INTERRUPT_EVENT_SCOPE} or
 * {@link ActivityStartBehavior#CONCURRENT_IN_FLOW_SCOPE}</p>
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 */
public class EventSubProcessStartEventActivityBehavior extends NoneStartEventActivityBehavior {

}
