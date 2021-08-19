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
package org.camunda.bpm.quarkus.engine.extension.event;

/**
 * Event class that is fired after Camunda engine startup.
 *
 * The event is fired after the Camunda engine has been bootstrapped,
 * so it can be used to perform deployments to the engine.
 *
 * This event is observed in the following way:
 *
 * <code><pre>
 *     void onCamundaEngineStart(@Observes CamundaEngineStartupEvent event) {
 *         LOGGER.info("The Camunda engine is started");
 *     }
 * </pre></code>
 */
public class CamundaEngineStartupEvent {
}