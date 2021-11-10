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
package org.camunda.bpm.engine.impl.batch.message;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;

public class MessageCorrelationBatchConfiguration extends BatchConfiguration {

  protected String messageName;

  public MessageCorrelationBatchConfiguration(List<String> ids,
                                     String messageName,
                                     String batchId) {
    this(ids, null, messageName, batchId);
  }

  public MessageCorrelationBatchConfiguration(List<String> ids,
                                     DeploymentMappings mappings,
                                     String messageName,
                                     String batchId) {
    super(ids, mappings);
    this.messageName = messageName;
    this.batchId = batchId;
  }

  public MessageCorrelationBatchConfiguration(List<String> ids,
                                     DeploymentMappings mappings,
                                     String messageName) {
    this(ids, mappings, messageName, null);
  }

  public String getMessageName() {
    return messageName;
  }

  public void setMessageName(String messageName) {
    this.messageName = messageName;;
  }

}
