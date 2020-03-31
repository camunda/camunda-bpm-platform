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

public class ModificationBatchConfiguration extends BatchConfiguration {

  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;
  protected String processDefinitionId;

  public ModificationBatchConfiguration(List<String> ids, String processDefinitionId, List<AbstractProcessInstanceModificationCommand> instructions,
      boolean skipCustomListeners, boolean skipIoMappings) {
    this(ids, null, processDefinitionId, instructions, skipCustomListeners, skipIoMappings);
  }

  public ModificationBatchConfiguration(List<String> ids, DeploymentMappings mappings,
      String processDefinitionId, List<AbstractProcessInstanceModificationCommand> instructions,
      boolean skipCustomListeners, boolean skipIoMappings) {
    super(ids, mappings);
    this.instructions = instructions;
    this.processDefinitionId = processDefinitionId;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

}
