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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.query.Query;

/**
 * @author Sebastian Menski
 */
public class ExecuteFilterCountCmd extends AbstractExecuteFilterCmd implements Command<Long> {

  private static final long serialVersionUID = 1L;

  public ExecuteFilterCountCmd(String filterId) {
    super(filterId);
  }

  public ExecuteFilterCountCmd(String filterId, Query<?, ?> extendingQuery) {
    super(filterId, extendingQuery);
  }

  public Long execute(CommandContext commandContext) {
    Filter filter = getFilter(commandContext);
    return filter.getQuery().count();
  }

}
