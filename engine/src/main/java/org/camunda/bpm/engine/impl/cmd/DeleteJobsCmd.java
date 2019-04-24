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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tom Baeyens
 */
public class DeleteJobsCmd implements Command<Void> {

  private static final long serialVersionUID = 1L;

  protected List<String> jobIds;
  protected boolean cascade;

  public DeleteJobsCmd(List<String> jobIds) {
    this(jobIds, false);
  }

  public DeleteJobsCmd(List<String> jobIds, boolean cascade) {
    this.jobIds = jobIds;
    this.cascade = cascade;
  }

  public DeleteJobsCmd(String jobId) {
    this(jobId, false);
  }

  public DeleteJobsCmd(String jobId, boolean cascade) {
    this.jobIds = new ArrayList<String>();
    jobIds.add(jobId);
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    JobEntity jobToDelete = null;
    for (String jobId: jobIds) {
      jobToDelete = Context
        .getCommandContext()
        .getJobManager()
        .findJobById(jobId);

      if(jobToDelete != null) {
        // When given job doesn't exist, ignore
        jobToDelete.delete();

        if (cascade) {
          commandContext
            .getHistoricJobLogManager()
            .deleteHistoricJobLogByJobId(jobId);
        }
      }
    }
    return null;
  }
}
