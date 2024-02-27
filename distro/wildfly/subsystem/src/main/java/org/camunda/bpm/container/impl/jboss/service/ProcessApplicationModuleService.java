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
package org.camunda.bpm.container.impl.jboss.service;

import java.util.function.Consumer;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * <p>Service installed for a process application module</p>
 *
 * <p>This service is used as a "root" service for all services installed by a
 * process application deployment, be it from a DeploymentProcessor or at Runtime.
 * As this service is installed as a child service on the deployment unit, it is
 * guaranteed that the undeployment operation removes all services installed by
 * the process application.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationModuleService implements Service<ServiceTarget> {

  protected ServiceTarget childTarget;
  protected Consumer<ProcessApplicationModuleService> provider;

  public ProcessApplicationModuleService(Consumer<ProcessApplicationModuleService> provider) {
    this.provider = provider;
  }

  @Override
  public ServiceTarget getValue() throws IllegalStateException, IllegalArgumentException {
    return childTarget;
  }

  @Override
  public void start(StartContext context) throws StartException {
    childTarget = context.getChildTarget();
    provider.accept(this);
  }

  @Override
  public void stop(StopContext context) {
    provider.accept(null);
  }


}
