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

import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.cmd.AbstractSetJobsRetriesBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class SetJobsRetriesByProcessBatchCmd extends AbstractSetJobsRetriesBatchCmd {

  protected final List<String> processInstanceIds;
  protected final ProcessInstanceQuery query;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;

  public SetJobsRetriesByProcessBatchCmd(List<String> processInstanceIds,
                                         ProcessInstanceQuery query,
                                         HistoricProcessInstanceQuery historicProcessInstanceQuery,
                                         int retries) {
    this.processInstanceIds = processInstanceIds;
    this.query = query;
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    this.retries = retries;
  }

  protected List<String> collectJobIds(CommandContext commandContext) {
    List<String> collectedJobIds = new ArrayList<String>();
    List<String> collectedProcessInstanceIds = new ArrayList<String>();

    if (query != null) {
      collectedProcessInstanceIds.addAll(((ProcessInstanceQueryImpl)query).listIds());
    }

    if (historicProcessInstanceQuery != null) {
      List<String> ids =
          ((HistoricProcessInstanceQueryImpl) historicProcessInstanceQuery).listIds();
      collectedProcessInstanceIds.addAll(ids);
    }

    if (this.processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(this.processInstanceIds);
    }

    for (String process : collectedProcessInstanceIds) {
      for (Job job : commandContext.getJobManager().findJobsByProcessInstanceId(process)) {
        collectedJobIds.add(job.getId());
      }
    }

    return collectedJobIds;
  }

}
