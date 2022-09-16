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

import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstanceBatchConfigurationJsonConverter
    extends AbstractBatchConfigurationObjectConverter<BatchConfiguration> {

  public static final DeleteHistoricProcessInstanceBatchConfigurationJsonConverter INSTANCE = new DeleteHistoricProcessInstanceBatchConfigurationJsonConverter();

  public static final String HISTORIC_PROCESS_INSTANCE_IDS = "historicProcessInstanceIds";
  public static final String HISTORIC_PROCESS_INSTANCE_ID_MAPPINGS = "historicProcessInstanceIdMappings";
  public static final String FAIL_IF_NOT_EXISTS = "failIfNotExists";

  @Override
  public JsonObject writeConfiguration(BatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();
    JsonUtil.addListField(json, HISTORIC_PROCESS_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addListField(json, HISTORIC_PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, FAIL_IF_NOT_EXISTS, configuration.isFailIfNotExists());
    return json;
  }

  @Override
  public BatchConfiguration readConfiguration(JsonObject json) {
    BatchConfiguration configuration = new BatchConfiguration(readProcessInstanceIds(json), readIdMappings(json),
        JsonUtil.getBoolean(json, FAIL_IF_NOT_EXISTS));
    return configuration;
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, HISTORIC_PROCESS_INSTANCE_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject json) {
    return JsonUtil.asList(JsonUtil.getArray(json, HISTORIC_PROCESS_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
