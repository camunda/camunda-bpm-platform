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
package org.camunda.bpm.engine.impl.batch;

import java.util.List;


public class BatchConfiguration {

  protected List<String> ids;
  protected DeploymentMappings idMappings;
  protected boolean failIfNotExists;
  protected String batchId;

  public BatchConfiguration(List<String> ids) {
    this(ids, true);
  }

  public BatchConfiguration(List<String> ids, boolean failIfNotExists) {
    this(ids, null, failIfNotExists);
  }

  public BatchConfiguration(List<String> ids, DeploymentMappings mappings) {
    this(ids, mappings, true);
  }

  public BatchConfiguration(List<String> ids, DeploymentMappings mappings, boolean failIfNotExists) {
    this.ids = ids;
    this.idMappings = mappings;
    this.failIfNotExists = failIfNotExists;
  }

  public BatchConfiguration(List<String> ids, DeploymentMappings mappings, String batchId) {
    this.ids = ids;
    this.idMappings = mappings;
    this.batchId = batchId;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  public DeploymentMappings getIdMappings() {
    return idMappings;
  }

  public void setIdMappings(DeploymentMappings idMappings) {
    this.idMappings = idMappings;
  }

  public boolean isFailIfNotExists() {
    return failIfNotExists;
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

}
