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
package org.camunda.bpm.engine.impl.cmd.batch;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.removaltime.SetRemovalTimeBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.SetRemovalTimeToHistoricProcessInstancesAsyncBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Tassilo Weidner
 */
public class SetRemovalTimeToHistoricProcessInstancesCmd extends AbstractIDBasedBatchCmd<Batch> {

  protected SetRemovalTimeToHistoricProcessInstancesAsyncBuilderImpl builder;

  public SetRemovalTimeToHistoricProcessInstancesCmd(SetRemovalTimeToHistoricProcessInstancesAsyncBuilderImpl builder) {
    this.builder = builder;
  }

  public Batch execute(CommandContext commandContext) {
    List<HistoricProcessInstance> historicProcessInstances = builder.getQuery().list();

    ensureNotEmpty(BadUserRequestException.class, "historicProcessInstances", historicProcessInstances);

    List<String> historicProcessInstanceIds = new ArrayList<>();
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      historicProcessInstanceIds.add(historicProcessInstance.getId());
    }

    BatchEntity batch = createBatch(commandContext, historicProcessInstanceIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> ids) {
    return new SetRemovalTimeBatchConfiguration(ids, builder.getRemovalTime(), builder.hasRemovalTime());
  }

  protected BatchJobHandler getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_PROCESS_SET_REMOVAL_TIME);
  }

}
