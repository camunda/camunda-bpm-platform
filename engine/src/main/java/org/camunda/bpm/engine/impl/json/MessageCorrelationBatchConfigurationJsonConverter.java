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
package org.camunda.bpm.engine.impl.json;

import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.batch.message.MessageCorrelationBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

import java.util.List;

public class MessageCorrelationBatchConfigurationJsonConverter
    extends AbstractBatchConfigurationObjectConverter<MessageCorrelationBatchConfiguration> {

  public static final MessageCorrelationBatchConfigurationJsonConverter INSTANCE = new MessageCorrelationBatchConfigurationJsonConverter();

  public static final String MESSAGE_NAME = "messageName";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String PROCESS_INSTANCE_ID_MAPPINGS = "processInstanceIdMappings";
  public static final String BATCH_ID = "batchId";

  @Override
  public JsonObject writeConfiguration(MessageCorrelationBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addField(json, MESSAGE_NAME, configuration.getMessageName());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addListField(json, PROCESS_INSTANCE_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addField(json, BATCH_ID, configuration.getBatchId());

    return json;
  }

  @Override
  public MessageCorrelationBatchConfiguration readConfiguration(JsonObject json) {
    return new MessageCorrelationBatchConfiguration(
        readProcessInstanceIds(json),
        readIdMappings(json),
        JsonUtil.getString(json, MESSAGE_NAME, null),
        JsonUtil.getString(json, BATCH_ID));
  }

  protected List<String> readProcessInstanceIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, PROCESS_INSTANCE_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject json) {
    return JsonUtil.asList(JsonUtil.getArray(json, PROCESS_INSTANCE_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
