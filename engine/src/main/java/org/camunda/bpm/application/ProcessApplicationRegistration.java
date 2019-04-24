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
package org.camunda.bpm.application;

import java.util.Set;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.repository.Deployment;


/**
 * <p>Represents a registration of a process application with a process engine</p>
 *
 * @author Daniel Meyer
 *
 * @see ManagementService#registerProcessApplication(String, ProcessApplicationReference)
 *
 */
public interface ProcessApplicationRegistration {

  /**
   * @return the id of the {@link Deployment} for which the registration was created
   */
  public Set<String> getDeploymentIds();

  /**
   * @return the name of the process engine to which the deployment was made
   */
  public String getProcessEngineName();


}
