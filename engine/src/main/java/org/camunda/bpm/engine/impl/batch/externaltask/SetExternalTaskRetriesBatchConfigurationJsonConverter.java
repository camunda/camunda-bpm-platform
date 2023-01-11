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
package org.camunda.bpm.engine.impl.batch.externaltask;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.JsonObject;

public class SetExternalTaskRetriesBatchConfigurationJsonConverter
    extends AbstractBatchConfigurationObjectConverter<SetRetriesBatchConfiguration> {

  public static final SetExternalTaskRetriesBatchConfigurationJsonConverter INSTANCE = new SetExternalTaskRetriesBatchConfigurationJsonConverter();

  public static final String EXTERNAL_TASK_IDS = "externalTaskIds";
  public static final String EXTERNAL_TASK_ID_MAPPINGS = "externalTaskIdMappingss";
  public static final String RETRIES = "retries";

  @Override
  public JsonObject writeConfiguration(SetRetriesBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addListField(json, EXTERNAL_TASK_IDS, configuration.getIds());
    JsonUtil.addListField(json, EXTERNAL_TASK_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addField(json, RETRIES, configuration.getRetries());

    return json;
  }

  @Override
  public SetRetriesBatchConfiguration readConfiguration(JsonObject json) {
    return new SetRetriesBatchConfiguration(readExternalTaskIds(json), readIdMappings(json), JsonUtil.getInt(json, RETRIES));
  }

  protected List<String> readExternalTaskIds(JsonObject json) {
    return JsonUtil.asStringList(JsonUtil.getArray(json, EXTERNAL_TASK_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject json) {
    return JsonUtil.asList(JsonUtil.getArray(json, EXTERNAL_TASK_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
