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
package com.camunda.fox.demo.twitter.jsf;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * backing bean for retrieving a list of processes
 */
@Named
@RequestScoped
public class ProcessList {
  
  private String processDefinitionKey; 

  @Inject
  private RepositoryService repositoryService;
  
  @Produces
  @Named("processDefinitionList")
  public List<ProcessDefinition> getProcessDefinitionList() {
    return repositoryService.createProcessDefinitionQuery()
            .list();
  }
  
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }
  
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

}
