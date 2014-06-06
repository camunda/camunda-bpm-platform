package org.camunda.bpm.engine.cdi.jsf;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@ConversationScoped
@Named("camundaTaskForm")
public class TaskForm implements Serializable {

  private static Logger log = Logger.getLogger(TaskForm.class.getName());

  private static final long serialVersionUID = 1L;

  protected String url;

  protected String processDefinitionId;
  protected String processDefinitionKey;

  @Inject
  protected BusinessProcess businessProcess;

  @Inject
  protected RepositoryService repositoryService;

  @Inject
  protected Instance<Conversation> conversationInstance;

  /**
   * @deprecated use {@link startTaskForm()} instead
   *
   * @param taskId
   * @param callbackUrl
   */
  @Deprecated
  public void startTask(String taskId, String callbackUrl) {
    if (taskId==null || callbackUrl == null) {
      if (FacesContext.getCurrentInstance().isPostback()) {
        // if this is an AJAX request ignore it, since we will receive multiple calls to this bean if it is added
        // as preRenderView event
        // see http://stackoverflow.com/questions/2830834/jsf-fevent-prerenderview-is-triggered-by-fajax-calls-and-partial-renders-some
        return;
      }
      // return it anyway but log an info message
      log.log(Level.INFO, "Called startTask method without proper parameter (taskId='"+taskId+"'; callbackUrl='"+callbackUrl+"') even if it seems we are not called by an AJAX Postback. Are you using the camundaTaskForm bean correctly?");
      return;
    }
    // Note that we always run in a conversation
    this.url = callbackUrl;
    businessProcess.startTask(taskId, true);
  }

  /**
   * Get taskId and callBackUrl from request and start a conversation
   * to start the form
   *
   */
  public void startTaskForm() {
    Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String taskId = requestParameterMap.get("taskId");
    String callbackUrl = requestParameterMap.get("callbackUrl");

    if (taskId==null || callbackUrl == null) {
      if (FacesContext.getCurrentInstance().isPostback()) {
        // if this is an AJAX request ignore it, since we will receive multiple calls to this bean if it is added
        // as preRenderView event
        // see http://stackoverflow.com/questions/2830834/jsf-fevent-prerenderview-is-triggered-by-fajax-calls-and-partial-renders-some
        return;
      }
      // return it anyway but log an info message
      log.log(Level.INFO, "Called startTask method without proper parameter (taskId='"+taskId+"'; callbackUrl='"+callbackUrl+"') even if it seems we are not called by an AJAX Postback. Are you using the camundaTaskForm bean correctly?");
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

  /**
   * @deprecated use {@link startProcessInstanceByIdForm()} instead
   *
   * @param processDefinitionId
   * @param callbackUrl
   */
  @Deprecated
  public void startProcessInstanceByIdForm(String processDefinitionId, String callbackUrl) {
    this.url = callbackUrl;
    this.processDefinitionId = processDefinitionId;
    beginConversation();
  }

  /**
   * Get processDefinitionId and callbackUrl from request and start a conversation
   * to start the form
   *
   */
  public void startProcessInstanceByIdForm() {
    if (FacesContext.getCurrentInstance().isPostback()) {
      // if this is an AJAX request ignore it, since we will receive multiple calls to this bean if it is added
      // as preRenderView event
      // see http://stackoverflow.com/questions/2830834/jsf-fevent-prerenderview-is-triggered-by-fajax-calls-and-partial-renders-some
      return;
    }

    Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String processDefinitionId = requestParameterMap.get("processDefinitionId");
    String callbackUrl = requestParameterMap.get("callbackUrl");
    this.url = callbackUrl;
    this.processDefinitionId = processDefinitionId;
    beginConversation();
  }

  /**
   * @deprecated use {@link startProcessInstanceByKeyForm()} instead
   *
   * @param processDefinitionKey
   * @param callbackUrl
   */
  @Deprecated
  public void startProcessInstanceByKeyForm(String processDefinitionKey, String callbackUrl) {
    this.url = callbackUrl;
    this.processDefinitionKey = processDefinitionKey;
    beginConversation();
  }

  /**
   * Get processDefinitionKey and callbackUrl from request and start a conversation
   * to start the form
   *
   */
  public void startProcessInstanceByKeyForm() {
    if (FacesContext.getCurrentInstance().isPostback()) {
      // if this is an AJAX request ignore it, since we will receive multiple calls to this bean if it is added
      // as preRenderView event
      // see http://stackoverflow.com/questions/2830834/jsf-fevent-prerenderview-is-triggered-by-fajax-calls-and-partial-renders-some
      return;
    }

    Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    String processDefinitionKey = requestParameterMap.get("processDefinitionKey");
    String callbackUrl = requestParameterMap.get("callbackUrl");
    this.url = callbackUrl;
    this.processDefinitionKey = processDefinitionKey;
    beginConversation();
  }

  public void completeProcessInstanceForm() throws IOException {
    // start the process instance
    if (processDefinitionId!=null) {
      businessProcess.startProcessById(processDefinitionId);
      processDefinitionId = null;
    } else {
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
