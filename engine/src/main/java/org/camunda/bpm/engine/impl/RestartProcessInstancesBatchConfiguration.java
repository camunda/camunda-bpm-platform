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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;

/**
 *
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesBatchConfiguration extends BatchConfiguration {

  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected String processDefinitionId;
  protected boolean initialVariables;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;
  protected boolean withoutBusinessKey;

  public RestartProcessInstancesBatchConfiguration(List<String> processInstanceIds,
      List<AbstractProcessInstanceModificationCommand> instructions, String processDefinitionId,
      boolean initialVariables, boolean skipCustomListeners, boolean skipIoMappings, boolean withoutBusinessKey) {
    this(processInstanceIds, null, instructions, processDefinitionId, initialVariables, skipCustomListeners, skipIoMappings, withoutBusinessKey);
  }

  public RestartProcessInstancesBatchConfiguration(List<String> processInstanceIds, DeploymentMappings mappings,
      List<AbstractProcessInstanceModificationCommand> instructions, String processDefinitionId,
      boolean initialVariables, boolean skipCustomListeners, boolean skipIoMappings, boolean withoutBusinessKey) {
    super(processInstanceIds, mappings);
    this.instructions = instructions;
    this.processDefinitionId = processDefinitionId;
    this.initialVariables = initialVariables;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
    this.withoutBusinessKey = withoutBusinessKey;
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions) {
    this.instructions = instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public boolean isInitialVariables() {
    return initialVariables;
  }

  public void setInitialVariables(boolean initialVariables) {
    this.initialVariables = initialVariables;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  public boolean isWithoutBusinessKey() {
    return withoutBusinessKey;
  }

  public void setWithoutBusinessKey(boolean withoutBusinessKey) {
    this.withoutBusinessKey = withoutBusinessKey;
  }
}
