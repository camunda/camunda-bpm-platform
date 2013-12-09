package org.camunda.bpm.engine.cdi.compat;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Named;

@ConversationScoped
@Named("fox.taskForm")
@Typed({FoxTaskForm.class})
public class FoxTaskForm extends org.camunda.bpm.engine.cdi.jsf.TaskForm {

  private static final long serialVersionUID = 9042602064970870095L;
}
