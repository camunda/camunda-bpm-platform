package org.camunda.bpm.engine.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.ModificationBatchConfiguration;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
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

    List<String> processInstanceIds = readProcessInstanceIds(json);
    String processDefinitionId = json.getString(PROCESS_DEFINITION_ID);
    List<AbstractProcessInstanceModificationCommand> instructions = JsonUtil.jsonArrayAsList(json.getJSONArray(INSTRUCTIONS),
        ModificationCmdJsonConverter.INSTANCE);
    boolean skipCustomListeners = json.getBoolean(SKIP_LISTENERS);
    boolean skipIoMappings = json.getBoolean(SKIP_IO_MAPPINGS);

    return new ModificationBatchConfiguration(
        processInstanceIds,
        processDefinitionId,
        instructions,
        skipCustomListeners,
        skipIoMappings);
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
