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

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.camunda.bpm.engine.impl.JobQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchElementConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.JobQuery;

/**
 * @author Askar Akhmerov
 */
public class SetJobsRetriesBatchCmd extends AbstractSetJobsRetriesBatchCmd {

  protected List<String> ids;
  protected JobQuery jobQuery;

  public SetJobsRetriesBatchCmd(List<String> ids, JobQuery jobQuery, int retries, Date dueDate, boolean isDueDateSet) {
    this.jobQuery = jobQuery;
    this.ids = ids;
    this.retries = retries;
    this.dueDate = dueDate;
    this.isDueDateSet = isDueDateSet;
  }

  protected BatchElementConfiguration collectJobIds(CommandContext commandContext) {
    BatchElementConfiguration elementConfiguration = new BatchElementConfiguration();

    if (!CollectionUtil.isEmpty(ids)) {
      JobQueryImpl query = new JobQueryImpl();
      query.jobIds(new HashSet<>(ids));
      elementConfiguration.addDeploymentMappings(
          commandContext.runWithoutAuthorization(query::listDeploymentIdMappings), ids);
    }

    if (jobQuery != null) {
      elementConfiguration.addDeploymentMappings(((JobQueryImpl) jobQuery).listDeploymentIdMappings());
    }

    return elementConfiguration;
  }

}
