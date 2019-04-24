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
package org.camunda.bpm.engine.test.bpmn.tasklistener.util;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;


/**
 * @author Falko Menge <falko.menge@camunda.com>
 */
public class AssigneeOverwriteFromVariable implements TaskListener {

  @SuppressWarnings("unchecked")
  public void notify(DelegateTask delegateTask) {
    // get mapping table from variable
    DelegateExecution execution = delegateTask.getExecution();
    Map<String, String> assigneeMappingTable = (Map<String, String>) execution.getVariable("assigneeMappingTable");
    
    // get assignee from process
    String assigneeFromProcessDefinition = delegateTask.getAssignee();
    
    // overwrite assignee if there is an entry in the mapping table
    if (assigneeMappingTable.containsKey(assigneeFromProcessDefinition)) {
      String assigneeFromMappingTable = assigneeMappingTable.get(assigneeFromProcessDefinition);
      delegateTask.setAssignee(assigneeFromMappingTable);
    }
  }
  
}
