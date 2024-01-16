package org.camunda.bpm.engine.test.bpmn.tasklistener.builtin;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.slf4j.Logger;

public class PreParseListener extends AbstractBpmnParseListener {

  Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  @Override
  public void parseUserTask(final Element userTaskElement, final ScopeImpl scope, final ActivityImpl activity) {
    getTaskDefinition(activity).addBuiltInTaskListener("create", new TaskListener() {

      @Override
      public void notify(DelegateTask delegateTask) {
        LOG.info("Executed task listener: PreParseListener");
      }
    });
  }

  private TaskDefinition getTaskDefinition(final ActivityImpl activity) {
    return ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
  }
}