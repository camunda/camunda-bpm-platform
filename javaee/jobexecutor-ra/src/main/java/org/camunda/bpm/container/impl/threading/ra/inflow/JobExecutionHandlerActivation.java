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
package org.camunda.bpm.container.impl.threading.ra.inflow;


import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.camunda.bpm.container.impl.threading.ra.JcaExecutorServiceConnector;

/**
 * Represents the activation of a {@link JobExecutionHandler}
 * 
 * @author Daniel Meyer
 * 
 */
public class JobExecutionHandlerActivation {

  protected JcaExecutorServiceConnector ra;

  protected JobExecutionHandlerActivationSpec spec;

  protected MessageEndpointFactory endpointFactory;

  public JobExecutionHandlerActivation() throws ResourceException {
    this(null, null, null);
  }

  public JobExecutionHandlerActivation(JcaExecutorServiceConnector ra, MessageEndpointFactory endpointFactory, JobExecutionHandlerActivationSpec spec) throws ResourceException {
    this.ra = ra;
    this.endpointFactory = endpointFactory;
    this.spec = spec;
  }

  public JobExecutionHandlerActivationSpec getActivationSpec() {
    return spec;
  }

  public MessageEndpointFactory getMessageEndpointFactory() {
    return endpointFactory;
  }

  public void start() throws ResourceException {
    // nothing to do here
  }

  public void stop() {
    // nothing to do here
  }

}
