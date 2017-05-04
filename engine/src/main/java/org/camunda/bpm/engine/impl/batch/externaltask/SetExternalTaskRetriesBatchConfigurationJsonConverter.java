package org.camunda.bpm.engine.impl.batch.externaltask;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class SetExternalTaskRetriesBatchConfigurationJsonConverter extends JsonObjectConverter<SetRetriesBatchConfiguration> {

  public static final SetExternalTaskRetriesBatchConfigurationJsonConverter INSTANCE = new SetExternalTaskRetriesBatchConfigurationJsonConverter();

  public static final String EXTERNAL_TASK_IDS = "externalTaskIds";
  public static final String RETRIES = "retries";
  
  @Override
  public JSONObject toJsonObject(SetRetriesBatchConfiguration configuration) {
    JSONObject json = new JSONObject();
    
    JsonUtil.addListField(json, EXTERNAL_TASK_IDS, configuration.getIds());
    JsonUtil.addField(json, RETRIES, configuration.getRetries());
    
    return json;
  }

  @Override
  public SetRetriesBatchConfiguration toObject(JSONObject json) {
    return new SetRetriesBatchConfiguration(readExternalTaskIds(json), json.optInt(RETRIES));
  }
  
  protected List<String> readExternalTaskIds(JSONObject json) {
    List<Object> objects = JsonUtil.jsonArrayAsList(json.getJSONArray(EXTERNAL_TASK_IDS));
    List<String> externalTaskIds = new ArrayList<String>();
    for (Object object : objects) {
      externalTaskIds.add(object.toString());
    }
    return externalTaskIds;
  }

}
