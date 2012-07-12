package com.camunda.fox.client.impl.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.BusinessProcess;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;

@ConversationScoped
@Named("fox.taskForm")
public class TaskForm implements Serializable {
  
  private static Logger log = Logger.getLogger(TaskForm.class.getName());

  private static final long serialVersionUID = 1L;

  private String url;

  private String processDefinitionId; 
  private String processDefinitionKey; 
  
  @Inject
  private BusinessProcess businessProcess;

  @Inject
  private RepositoryService repositoryService;

  @Inject
  private Instance<Conversation> conversationInstance;

  public void startTask(String taskId, String callbackUrl) {
    if (taskId==null || callbackUrl == null) {
      if (FacesContext.getCurrentInstance().isPostback()) {
        // if this is an AJAX request ignore it, since we will receive multiple calls to this bean if it is added
        // as preRenderView event
        // see http://stackoverflow.com/questions/2830834/jsf-fevent-prerenderview-is-triggered-by-fajax-calls-and-partial-renders-some
        return;
      }
      // return it anyway but log an info message
      log.log(Level.INFO, "Called startTask method without proper parameter (taskId='"+taskId+"'; callbackUrl='"+callbackUrl+"') even if it seems we are not called by an AJAX Postback. Are you using the fox.taskForm bean corrctly?");
      return;
    }
    // Note that we always run in a conversation
    this.url = callbackUrl;
    businessProcess.startTask(taskId, true);
  }

  public void completeTask() throws IOException {
    // the conversation is always ended on task completion (otherwise the
    // redirect will end up in an exception anyway!)
    businessProcess.completeTask(true);
    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
  }

  private void beginConversation() {
    if (conversationInstance.get().isTransient()) {
      conversationInstance.get().begin();
    }
  }
  
  public void startProcessInstanceByIdForm(String processDefinitionId, String callbackUrl) {
    this.url = callbackUrl;
    this.processDefinitionId = processDefinitionId;
    beginConversation();
  }

  public void startProcessInstanceByKeyForm(String processDefinitionKey, String callbackUrl) {
    this.url = callbackUrl;
    this.processDefinitionKey = processDefinitionKey;    
    beginConversation();
  }

  public void completeProcessInstanceForm() throws IOException {
    // start the process instance
    if (processDefinitionId!=null) {
      businessProcess.startProcessById(processDefinitionId);
      processDefinitionId = null;
    }
    else {
      businessProcess.startProcessByKey(processDefinitionKey);
      processDefinitionKey = null;
    }
    
    // End the conversation   
    conversationInstance.get().end();
    
    // and redirect
    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
  }
  
  public ProcessDefinition getProcessDefinition() {
    // TODO cache result to avoid multiple queries within one page request
    if (processDefinitionId!=null) {
      return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    } else {
      return repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();      
    }
  }
  
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

}
