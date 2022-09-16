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
package org.camunda.bpm.engine.impl.dmn.batch;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

public class DeleteHistoricDecisionInstanceBatchConfigurationJsonConverter extends AbstractBatchConfigurationObjectConverter<BatchConfiguration> {

  public static final DeleteHistoricDecisionInstanceBatchConfigurationJsonConverter INSTANCE = new DeleteHistoricDecisionInstanceBatchConfigurationJsonConverter();

  public static final String HISTORIC_DECISION_INSTANCE_IDS = "historicDecisionInstanceIds";
  public static final String HISTORIC_DECISION_INSTANCE_ID_MAPPINGS = "historicDecisionInstanceIdMappingss";

  @Override
  public JsonObject writeConfiguration(BatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();
    JsonUtil.addListField(json, HISTORIC_DECISION_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addListField(json, HISTORIC_DECISION_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    return json;
  }

  @Override
  public BatchConfiguration readConfiguration(JsonObject json) {
    BatchConfiguration configuration = new BatchConfiguration(readDecisionInstanceIds(json), readMappings(json));
    return configuration;
  }

  protected List<String> readDecisionInstanceIds(JsonObject jsonNode) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonNode, HISTORIC_DECISION_INSTANCE_IDS));
  }

  protected DeploymentMappings readMappings(JsonObject jsonNode) {
    return JsonUtil.asList(JsonUtil.getArray(jsonNode, HISTORIC_DECISION_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }

}
