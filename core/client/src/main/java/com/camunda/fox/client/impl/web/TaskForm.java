package com.camunda.fox.client.impl.web;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.BusinessProcess;

@ConversationScoped
@Named
public class TaskForm implements Serializable {

  private static final long serialVersionUID = 1L;

  private String url;
  
  @Inject
  private BusinessProcess businessProcess;
  
  public void startTask(String taskId, String callbackUrl) {
    // Note that we always run in a conversation
    this.url = callbackUrl;
    businessProcess.startTask(taskId, true);
  }

  public void completeTask() throws IOException {
    // the conversation is always ended on task completion (otherwise the redirect will end up in an exception anyway!)
    businessProcess.completeTask(true);
    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
  }
    
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

}
