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

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class SetExternalTasksRetriesCmd extends AbstractSetExternalTaskRetriesCmd<Void> {

  public SetExternalTasksRetriesCmd(UpdateExternalTaskRetriesBuilderImpl builder) {
    super(builder);
  }

  @Override
  public Void execute(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = collectExternalTaskIds(commandContext);
    List<String> collectedIds = elementConfiguration.getIds();
    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", collectedIds);

    int instanceCount = collectedIds.size();
    writeUserOperationLog(commandContext, instanceCount, false);

    int retries = builder.getRetries();
    for (String externalTaskId : collectedIds) {
      new SetExternalTaskRetriesCmd(externalTaskId, retries, false)
          .execute(commandContext);
    }

    return null;
  }

}
