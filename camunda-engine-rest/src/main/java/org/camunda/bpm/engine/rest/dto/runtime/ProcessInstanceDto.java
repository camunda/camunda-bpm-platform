package org.camunda.bpm.engine.rest.dto.runtime;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.ProcessInstanceService;
import org.camunda.bpm.engine.rest.dto.AtomLink;
import org.camunda.bpm.engine.rest.dto.LinkableDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessInstanceDto extends LinkableDto {
  
  private String id;  
  private String definitionId;
  private String businessKey;
  private boolean ended;
  private boolean suspended;

  public String getId() {
    return id;
  }
  
  public String getDefinitionId() {
    return definitionId;
  }
  
  public String getBusinessKey() {
    return businessKey;
  }
  
  public boolean isEnded() {
    return ended;
  }
  
  public boolean isSuspended() {
    return suspended;
  }
  
  public static ProcessInstanceDto fromProcessInstance(ProcessInstance instance) {
    ProcessInstanceDto result = new ProcessInstanceDto();
    result.id = instance.getId();
    result.definitionId = instance.getProcessDefinitionId();
    result.businessKey = instance.getBusinessKey();
    result.ended = instance.isEnded();
    result.suspended = instance.isSuspended();
    return result;
  }

  @Override
  public AtomLink generateLink(UriInfo context, String action, String relation) {
    URI baseUri = context.getBaseUri();
    UriBuilder builder = UriBuilder.fromUri(baseUri).path(ProcessInstanceService.class).path("{id}");
    if (action != null) {
      builder.path(action);
    }
    
    URI linkUri = builder.build(id);
    AtomLink link = new AtomLink(relation, linkUri.toString());
    return link;
  }
}
