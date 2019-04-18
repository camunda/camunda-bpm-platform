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
package org.camunda.bpm.engine.test.api.authorization.service;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.identity.Authentication;

/**
 * @author Roman Smirnov
 *
 */
public abstract class MyDelegationService {

  public static Authentication CURRENT_AUTHENTICATION;
  public static Long INSTANCES_COUNT;

  // fetch current authentication //////////////////////////////////////////

  public void logAuthentication(DelegateExecution execution) {
    logAuthentication(execution.getProcessEngineServices());
  }

  public void logAuthentication(DelegateTask task) {
    logAuthentication(task.getProcessEngineServices());
  }

  protected void logAuthentication(ProcessEngineServices services) {
    IdentityService identityService = services.getIdentityService();
    logAuthentication(identityService);
  }

  protected void logAuthentication(IdentityService identityService) {
    CURRENT_AUTHENTICATION = identityService.getCurrentAuthentication();
  }

  // execute a query /////////////////////////////////////////////////////////

  public void logInstancesCount(DelegateExecution execution) {
    logInstancesCount(execution.getProcessEngineServices());
  }

  public void logInstancesCount(DelegateTask task) {
    logInstancesCount(task.getProcessEngineServices());
  }

  protected void logInstancesCount(ProcessEngineServices services) {
    RuntimeService runtimeService = services.getRuntimeService();
    logInstancesCount(runtimeService);
  }

  protected void logInstancesCount(RuntimeService runtimeService) {
    INSTANCES_COUNT = runtimeService.createProcessInstanceQuery().count();
  }

  // execute a command ///////////////////////////////////////////////////////

  public void executeCommand(DelegateExecution execution) {
    executeCommand(execution.getProcessEngineServices());
  }

  public void executeCommand(DelegateTask task) {
    executeCommand(task.getProcessEngineServices());
  }

  protected void executeCommand(ProcessEngineServices services) {
    RuntimeService runtimeService = services.getRuntimeService();
    executeCommand(runtimeService);
  }

  protected void executeCommand(RuntimeService runtimeService) {
    runtimeService.startProcessInstanceByKey("process");
  }

  // helper /////////////////////////////////////////////////////////////////

  public static void clearProperties() {
    CURRENT_AUTHENTICATION = null;
    INSTANCES_COUNT = null;
  }

}
