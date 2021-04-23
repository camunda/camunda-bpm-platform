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
package org.camunda.bpm.engine.impl.migration;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanImpl implements MigrationPlan {

  protected String sourceProcessDefinitionId;
  protected String targetProcessDefinitionId;

  protected List<MigrationInstruction> instructions;

  protected VariableMap variables;

  public MigrationPlanImpl(String sourceProcessDefinitionId, String targetProcessDefinitionId) {
    this.sourceProcessDefinitionId = sourceProcessDefinitionId;
    this.targetProcessDefinitionId = targetProcessDefinitionId;
    this.instructions = new ArrayList<MigrationInstruction>();
  }

  public String getSourceProcessDefinitionId() {
    return sourceProcessDefinitionId;
  }

  public void setSourceProcessDefinitionId(String sourceProcessDefinitionId) {
    this.sourceProcessDefinitionId = sourceProcessDefinitionId;
  }

  public String getTargetProcessDefinitionId() {
    return targetProcessDefinitionId;
  }

  @Override
  public VariableMap getVariables() {
    return variables;
  }

  public void setVariables(VariableMap variables) {
    this.variables = variables;
  }

  public void setTargetProcessDefinitionId(String targetProcessDefinitionId) {
    this.targetProcessDefinitionId = targetProcessDefinitionId;
  }

  public List<MigrationInstruction> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<MigrationInstruction> instructions) {
    this.instructions = instructions;
  }

  public String toString() {
    return "MigrationPlan[" +
      "sourceProcessDefinitionId='" + sourceProcessDefinitionId + '\'' +
      ", targetProcessDefinitionId='" + targetProcessDefinitionId + '\'' +
      ", instructions=" + instructions +
      ']';
  }

}
