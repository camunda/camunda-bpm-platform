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

package org.camunda.bpm.engine.impl.externaltask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.externaltask.FetchAndLockBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class FetchAndLockBuilderImpl implements FetchAndLockBuilder {

  protected CommandExecutor commandExecutor;

  protected String workerId;
  protected int maxTasks;

  protected boolean usePriority;

  protected Map<String, TopicFetchInstruction> instructions;
  protected TopicFetchInstruction currentInstruction;

  protected Direction createTimeDirection;
  protected boolean useCreateTime;

  public FetchAndLockBuilderImpl() {
  }

  public FetchAndLockBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    this.instructions = new HashMap<>();
  }

  @Override
  public FetchAndLockBuilder topic(String topicName, long lockDuration) {
    submitCurrentInstruction();
    currentInstruction = new TopicFetchInstruction(topicName, lockDuration);
    return this;
  }

  public List<LockedExternalTask> execute() {
    submitCurrentInstruction();
    return commandExecutor.execute(new FetchExternalTasksCmd(workerId, maxTasks, instructions, usePriority, useCreateTime, createTimeDirection));
  }

  public FetchAndLockBuilderImpl workerId(String workerId) {
    this.workerId = workerId;
    return this;
  }

  public FetchAndLockBuilderImpl maxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
    return this;
  }

  public FetchAndLockBuilderImpl usePriority(boolean usePriority) {
    this.usePriority = usePriority;
    return this;
  }

  public FetchAndLockBuilderImpl variables(String... variables) {
    // don't use plain Arrays.asList since this returns an instance of a different list class
    // that is private and may mess mybatis queries up
    if (variables != null) {
      currentInstruction.setVariablesToFetch(new ArrayList<>(Arrays.asList(variables)));
    }
    return this;
  }

  public FetchAndLockBuilderImpl variables(List<String> variables) {
    currentInstruction.setVariablesToFetch(variables);
    return this;
  }

  public FetchAndLockBuilderImpl processInstanceVariableEquals(Map<String, Object> variables) {
    currentInstruction.setFilterVariables(variables);
    return this;
  }

  public FetchAndLockBuilderImpl processInstanceVariableEquals(String name, Object value) {
    currentInstruction.addFilterVariable(name, value);
    return this;
  }

  public FetchAndLockBuilderImpl businessKey(String businessKey) {
    currentInstruction.setBusinessKey(businessKey);
    return this;
  }

  public FetchAndLockBuilderImpl processDefinitionId(String processDefinitionId) {
    currentInstruction.setProcessDefinitionId(processDefinitionId);
    return this;
  }

  public FetchAndLockBuilderImpl processDefinitionIdIn(String... processDefinitionIds) {
    currentInstruction.setProcessDefinitionIds(processDefinitionIds);
    return this;
  }

  public FetchAndLockBuilderImpl processDefinitionKey(String processDefinitionKey) {
    currentInstruction.setProcessDefinitionKey(processDefinitionKey);
    return this;
  }

  public FetchAndLockBuilderImpl processDefinitionKeyIn(String... processDefinitionKeys) {
    currentInstruction.setProcessDefinitionKeys(processDefinitionKeys);
    return this;
  }

  public FetchAndLockBuilderImpl processDefinitionVersionTag(String processDefinitionVersionTag) {
    currentInstruction.setProcessDefinitionVersionTag(processDefinitionVersionTag);
    return this;
  }

  public FetchAndLockBuilderImpl withoutTenantId() {
    currentInstruction.setTenantIds(null);
    return this;
  }

  public FetchAndLockBuilderImpl tenantIdIn(String... tenantIds) {
    currentInstruction.setTenantIds(tenantIds);
    return this;
  }

  protected void submitCurrentInstruction() {
    if (currentInstruction != null) {
      this.instructions.put(currentInstruction.getTopicName(), currentInstruction);
    }
  }

  public FetchAndLockBuilderImpl enableCustomObjectDeserialization() {
    currentInstruction.setDeserializeVariables(true);
    return this;
  }

  public FetchAndLockBuilderImpl localVariables() {
    currentInstruction.setLocalVariables(true);
    return this;
  }

  public FetchAndLockBuilderImpl includeExtensionProperties() {
    currentInstruction.setIncludeExtensionProperties(true);
    return this;
  }

  public FetchAndLockBuilderImpl orderByCreateTime() {
    useCreateTime = true;
    return this;
  }

  public FetchAndLockBuilderImpl asc() {
    createTimeDirection = Direction.ASCENDING;
    return this;
  }
  public FetchAndLockBuilderImpl desc() {
    createTimeDirection = Direction.DESCENDING;
    return this;
  }

}
