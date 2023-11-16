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

import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.FetchAndLockBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

public class FetchAndLockBuilderImpl implements FetchAndLockBuilder {

  protected final CommandExecutor commandExecutor;

  protected String workerId;
  protected int maxTasks;

  protected boolean usePriority;

  protected Direction createTimeDirection;
  protected boolean useCreateTime;

  protected ExternalTaskQueryTopicBuilderImpl topicsConfigBuilder;

  public FetchAndLockBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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

  @Override
  public ExternalTaskQueryTopicBuilder subscribe() {
    topicsConfigBuilder = new ExternalTaskQueryTopicBuilderImpl(commandExecutor, workerId, maxTasks, usePriority,
        useCreateTime, createTimeDirection, new HashMap<>());

    return topicsConfigBuilder;
  }

  @Override
  public List<LockedExternalTask> execute() {
    var builder = new ExternalTaskQueryTopicBuilderImpl(topicsConfigBuilder);
    return builder.execute();
  }

}
