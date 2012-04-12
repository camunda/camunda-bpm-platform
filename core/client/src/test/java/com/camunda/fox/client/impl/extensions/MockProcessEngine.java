/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl.extensions;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;


public class MockProcessEngine implements ProcessEngine {
  
  private final String name;

  public MockProcessEngine(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  @Override
  public RepositoryService getRepositoryService() {
    return null;
  }

  @Override
  public RuntimeService getRuntimeService() {
    return null;
  }

  @Override
  public FormService getFormService() {
    return null;
  }

  @Override
  public TaskService getTaskService() {
    return null;
  }

  @Override
  public HistoryService getHistoryService() {
    return null;
  }

  @Override
  public IdentityService getIdentityService() {
    return null;
  }

  @Override
  public ManagementService getManagementService() {
    return null;
  }

  @Override
  public void close() {    
  }
}
