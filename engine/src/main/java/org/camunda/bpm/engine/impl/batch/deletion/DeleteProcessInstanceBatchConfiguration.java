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
package org.camunda.bpm.engine.impl.batch.deletion;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;

/**
 * Configuration object that is passed to the Job that will actually perform execution of
 * deletion.
 * <p>
 * This object will be serialized and persisted as run will be performed asynchronously.
 *
 * @author Askar Akhmerov
 * @see org.camunda.bpm.engine.impl.batch.deletion.DeleteProcessInstanceBatchConfigurationJsonConverter
 */
public class DeleteProcessInstanceBatchConfiguration extends BatchConfiguration {
  protected String deleteReason;
  protected boolean skipCustomListeners;
  protected boolean skipSubprocesses;

  public DeleteProcessInstanceBatchConfiguration(List<String> ids, DeploymentMappings mappings, boolean skipCustomListeners, boolean skipSubprocesses) {
    this(ids, mappings, null, skipCustomListeners, skipSubprocesses, true);
  }

  public DeleteProcessInstanceBatchConfiguration(List<String> ids, DeploymentMappings mappings, String deleteReason, boolean skipCustomListeners) {
    this(ids, mappings, deleteReason, skipCustomListeners, true, true);
  }

  public DeleteProcessInstanceBatchConfiguration(List<String> ids, DeploymentMappings mappings, String deleteReason, boolean skipCustomListeners, boolean skipSubprocesses, boolean failIfNotExists) {
    super(ids, mappings);
    this.deleteReason = deleteReason;
    this.skipCustomListeners = skipCustomListeners;
    this.skipSubprocesses = skipSubprocesses;
    this.failIfNotExists = failIfNotExists;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipSubprocesses() {
    return skipSubprocesses;
  }

  public void setSkipSubprocesses(boolean skipSubprocesses) {
    this.skipSubprocesses = skipSubprocesses;
  }

}
