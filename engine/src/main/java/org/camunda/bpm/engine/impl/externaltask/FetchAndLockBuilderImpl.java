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

import static org.camunda.bpm.engine.impl.Direction.ASCENDING;
import static org.camunda.bpm.engine.impl.Direction.DESCENDING;
import static org.camunda.bpm.engine.impl.ExternalTaskQueryProperty.CREATE_TIME;
import static org.camunda.bpm.engine.impl.util.CollectionUtil.getLastElement;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.FetchAndLockBuilder;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * Implementation of {@link FetchAndLockBuilder}.
 */
public class FetchAndLockBuilderImpl implements FetchAndLockBuilder {

  protected final CommandExecutor commandExecutor;

  protected String workerId;
  protected int maxTasks;

  protected boolean usePriority;

  protected List<QueryOrderingProperty> orderingProperties = new ArrayList<>();

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
    orderingProperties.add(new QueryOrderingProperty(CREATE_TIME, null));
    return this;
  }

  public FetchAndLockBuilderImpl asc() throws NotValidException {
    configureLastOrderingPropertyDirection(ASCENDING);
    return this;
  }

  public FetchAndLockBuilderImpl desc() throws NotValidException {
    configureLastOrderingPropertyDirection(DESCENDING);
    return this;
  }

  @Override
  public ExternalTaskQueryTopicBuilder subscribe() {
    checkQueryOk();
    return new ExternalTaskQueryTopicBuilderImpl(commandExecutor, workerId, maxTasks, usePriority, orderingProperties);
  }

  protected void configureLastOrderingPropertyDirection(Direction direction) {
    QueryOrderingProperty lastProperty = !orderingProperties.isEmpty() ? getLastElement(orderingProperties) : null;

    ensureNotNull(NotValidException.class, "You should call any of the orderBy methods first before specifying a direction", "currentOrderingProperty", lastProperty);

    if (lastProperty.getDirection() != null) {
      ensureNull(NotValidException.class, "Invalid query: can specify only one direction desc() or asc() for an ordering constraint", "direction", direction);
    }

    lastProperty.setDirection(direction);
  }

  protected void checkQueryOk() {
    for (QueryOrderingProperty orderingProperty : orderingProperties) {
      ensureNotNull(NotValidException.class, "Invalid query: call asc() or desc() after using orderByXX()", "direction", orderingProperty.getDirection());
    }
  }
}