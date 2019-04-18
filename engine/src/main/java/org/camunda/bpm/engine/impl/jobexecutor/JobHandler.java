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
package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;


/**
 * @author Tom Baeyens
 */
public interface JobHandler<T extends JobHandlerConfiguration> {

  String getType();

  void execute(T configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId);

  T newConfiguration(String canonicalString);

  /**
   * Clean up before job is deleted. Like removing of auxiliary entities specific for this job handler.
   *
   * @param configuration the job handler configuration
   * @param jobEntity the job entity to be deleted
   */
  void onDelete(T configuration, JobEntity jobEntity);

}
