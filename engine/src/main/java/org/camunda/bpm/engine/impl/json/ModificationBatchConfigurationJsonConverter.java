package org.camunda.bpm.engine.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class ModificationBatchConfigurationJsonConverter extends JsonObjectConverter<ModificationBatchConfiguration>{

  public static final ModificationBatchConfigurationJsonConverter INSTANCE = new ModificationBatchConfigurationJsonConverter();
  public static final String INSTRUCTIONS = "instructions";
  public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
  public static final String SKIP_LISTENERS = "skipListeners";
  public static final String SKIP_IO_MAPPINGS = "skipIoMappings";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";

  @Override
  public JSONObject toJsonObject(ModificationBatchConfiguration configuration) {
    JSONObject json = new JSONObject();

    JsonUtil.addListField(json, INSTRUCTIONS, ModificationCmdJsonConverter.INSTANCE, configuration.getInstructions());
    JsonUtil.addListField(json, PROCESS_INSTANCE_IDS, configuration.getIds());
    JsonUtil.addField(json, PROCESS_DEFINITION_ID, configuration.getProcessDefinitionId());
    JsonUtil.addField(json, SKIP_LISTENERS, configuration.isSkipCustomListeners());
    JsonUtil.addField(json, SKIP_IO_MAPPINGS, configuration.isSkipIoMappings());

    return json;
  }

  @Override
  public ModificationBatchConfiguration toObject(JSONObject json) {
    ModificationBatchConfiguration configuration = new ModificationBatchConfiguration(readProcessInstanceIds(json), json.getString(PROCESS_DEFINITION_ID));

    configuration.setInstructions(JsonUtil.jsonArrayAsList(json.getJSONArray(INSTRUCTIONS), ModificationCmdJsonConverter.INSTANCE));
    configuration.setSkipCustomListeners(json.getBoolean(SKIP_LISTENERS));
    configuration.setSkipIoMappings(json.getBoolean(SKIP_IO_MAPPINGS));

    return configuration;
  }

  protected List<String> readProcessInstanceIds(JSONObject jsonObject) {
    List<Object> objects = JsonUtil.jsonArrayAsList(jsonObject.getJSONArray(PROCESS_INSTANCE_IDS));
    List<String> processInstanceIds = new ArrayList<String>();
    for (Object object : objects) {
      processInstanceIds.add((String) object);
    }
    return processInstanceIds;
  }

}
