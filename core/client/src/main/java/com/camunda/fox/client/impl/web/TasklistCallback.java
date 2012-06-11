
import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.cdi.BusinessProcess;

@ConversationScoped
@Named
public class TasklistCallback implements Serializable {

  private static final long serialVersionUID = 1L;

  private String url;
  
  @Inject
  private BusinessProcess businessProcess;

  public void completeTask(boolean endConversation) throws IOException {
    // TODO: Verify conversation state (remove parameter from method?) since 
    // with running conversation this results in an exception:
    // 
    businessProcess.completeTask(endConversation);
    FacesContext.getCurrentInstance().getExternalContext().redirect(url);
  }
    
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

}
