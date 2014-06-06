package org.camunda.bpm.engine.cdi.compat;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Named;

import org.camunda.bpm.engine.cdi.jsf.TaskForm;

@ConversationScoped
@Named("camunda.taskForm")
@Typed({CamundaTaskForm.class})
public class CamundaTaskForm extends TaskForm {

  private static final long serialVersionUID = 9042602064970870095L;
}
