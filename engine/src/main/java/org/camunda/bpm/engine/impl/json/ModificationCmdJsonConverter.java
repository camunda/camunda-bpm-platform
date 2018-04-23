package org.camunda.bpm.engine.impl.json;

import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityBeforeInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstanceCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class ModificationCmdJsonConverter extends JsonObjectConverter<AbstractProcessInstanceModificationCommand> {

  public static final ModificationCmdJsonConverter INSTANCE = new ModificationCmdJsonConverter();

  public static final String START_BEFORE = "startBeforeActivity";
  public static final String START_AFTER = "startAfterActivity";
  public static final String START_TRANSITION = "startTransition";
  public static final String CANCEL_ALL = "cancelAllForActivity";
  public static final String CANCEL_CURRENT = "cancelCurrentActiveActivityInstances";
  public static final String CANCEL_ACTIVITY_INSTANCES = "cancelActivityInstances";
  public static final String PROCESS_INSTANCE = "processInstances";
  public static final String CANCEL_TRANSITION_INSTANCES = "cancelTransitionInstances";

  @Override
  public JSONObject toJsonObject(AbstractProcessInstanceModificationCommand command) {
    JSONObject json = new JSONObject();

    if (command instanceof ActivityAfterInstantiationCmd) {
      JsonUtil.addField(json, START_AFTER, ((ActivityAfterInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof ActivityBeforeInstantiationCmd) {
      JsonUtil.addField(json, START_BEFORE, ((ActivityBeforeInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof TransitionInstantiationCmd) {
      JsonUtil.addField(json, START_TRANSITION, ((TransitionInstantiationCmd) command).getTargetElementId());
    }
    else if (command instanceof ActivityCancellationCmd) {
      JsonUtil.addField(json, CANCEL_ALL, ((ActivityCancellationCmd) command).getActivityId());
      JsonUtil.addField(json, CANCEL_CURRENT, ((ActivityCancellationCmd) command).isCancelCurrentActiveActivityInstances());
    }
    else if (command instanceof ActivityInstanceCancellationCmd) {
      JsonUtil.addField(json, CANCEL_ACTIVITY_INSTANCES, ((ActivityInstanceCancellationCmd) command).getActivityInstanceId());
      JsonUtil.addField(json, PROCESS_INSTANCE, ((ActivityInstanceCancellationCmd) command).getProcessInstanceId());
    }
    else if (command instanceof TransitionInstanceCancellationCmd) {
      JsonUtil.addField(json, CANCEL_TRANSITION_INSTANCES, ((TransitionInstanceCancellationCmd) command).getTransitionInstanceId());
      JsonUtil.addField(json, PROCESS_INSTANCE, ((TransitionInstanceCancellationCmd) command).getProcessInstanceId());
    }

    return json;
  }

  @Override
  public AbstractProcessInstanceModificationCommand toObject(JSONObject json) {

    AbstractProcessInstanceModificationCommand cmd = null;

    if (json.has(START_BEFORE)) {
      cmd = new ActivityBeforeInstantiationCmd(json.getString(START_BEFORE));
    }
    else if (json.has(START_AFTER)) {
      cmd = new ActivityAfterInstantiationCmd(json.getString(START_AFTER));
    }
    else if (json.has(START_TRANSITION)) {
      cmd = new TransitionInstantiationCmd(json.getString(START_TRANSITION));
    }
    else if (json.has(CANCEL_ALL)) {
      cmd = new ActivityCancellationCmd(json.getString(CANCEL_ALL));
      boolean cancelCurrentActiveActivityInstances = json.getBoolean(CANCEL_CURRENT);
      ((ActivityCancellationCmd) cmd).setCancelCurrentActiveActivityInstances(cancelCurrentActiveActivityInstances);
    }
    else if (json.has(CANCEL_ACTIVITY_INSTANCES)) {
      cmd = new ActivityInstanceCancellationCmd(json.getString(PROCESS_INSTANCE), json.getString(CANCEL_ACTIVITY_INSTANCES));
    }
    else if (json.has(CANCEL_TRANSITION_INSTANCES)) {
      cmd = new TransitionInstanceCancellationCmd(json.getString(PROCESS_INSTANCE), json.getString(CANCEL_TRANSITION_INSTANCES));
    }

    return cmd;
  }

}
