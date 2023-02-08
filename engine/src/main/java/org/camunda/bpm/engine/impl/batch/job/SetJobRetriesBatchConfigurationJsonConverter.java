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
package org.camunda.bpm.engine.impl.batch.job;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.impl.batch.AbstractBatchConfigurationObjectConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappingJsonConverter;
import org.camunda.bpm.engine.impl.batch.DeploymentMappings;
import org.camunda.bpm.engine.impl.batch.SetJobRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author Askar Akhmerov
 */
public class SetJobRetriesBatchConfigurationJsonConverter
    extends AbstractBatchConfigurationObjectConverter<SetJobRetriesBatchConfiguration> {

  public static final SetJobRetriesBatchConfigurationJsonConverter INSTANCE = new SetJobRetriesBatchConfigurationJsonConverter();

  public static final String JOB_IDS = "jobIds";
  public static final String JOB_ID_MAPPINGS = "jobIdMappings";
  public static final String RETRIES = "retries";
  public static final String DUE_DATE = "dueDate";

  @Override
  public JsonObject writeConfiguration(SetJobRetriesBatchConfiguration configuration) {
    JsonObject json = JsonUtil.createObject();

    JsonUtil.addListField(json, JOB_IDS, configuration.getIds());
    JsonUtil.addListField(json, JOB_ID_MAPPINGS, DeploymentMappingJsonConverter.INSTANCE, configuration.getIdMappings());
    JsonUtil.addField(json, RETRIES, configuration.getRetries());
    if(configuration.isDueDateSet()) {
      Date dueDate = configuration.getDueDate();
      if (dueDate == null) {
        JsonUtil.addNullField(json, DUE_DATE);
      } else {
        JsonUtil.addDateField(json, DUE_DATE, dueDate);
      }
    }
    return json;
  }

  @Override
  public SetJobRetriesBatchConfiguration readConfiguration(JsonObject json) {
    boolean isDueDateSet = json.has(DUE_DATE);
    Date dueDate = null;
    if (isDueDateSet && !json.get(DUE_DATE).isJsonNull()) {
      dueDate = new Date(JsonUtil.getLong(json, DUE_DATE));
    }

    SetJobRetriesBatchConfiguration configuration = new SetJobRetriesBatchConfiguration(
        readJobIds(json), readIdMappings(json), JsonUtil.getInt(json, RETRIES), dueDate, isDueDateSet);

    return configuration;
  }

  protected List<String> readJobIds(JsonObject jsonObject) {
    return JsonUtil.asStringList(JsonUtil.getArray(jsonObject, JOB_IDS));
  }

  protected DeploymentMappings readIdMappings(JsonObject jsonObject) {
    return JsonUtil.asList(JsonUtil.getArray(jsonObject, JOB_ID_MAPPINGS), DeploymentMappingJsonConverter.INSTANCE, DeploymentMappings::new);
  }
}
