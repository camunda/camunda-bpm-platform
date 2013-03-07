package org.camunda.bpm.cycle.web.jaxrs.ext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.cycle.web.dto.WebExceptionDTO;
import org.camunda.bpm.security.web.util.WebUtil;
import org.camunda.bpm.web.WebException;


/**
 *
 * @author nico.rehwaldt
 */
@Provider
public class WebExceptionMapper implements ExceptionMapper<WebException> {

  @Context
  private HttpServletRequest request;
  
  @Override
  public Response toResponse(WebException exception) {
    ResponseBuilder builder = JaxRsUtil.createResponse().status(exception.getStatus());
    if (WebUtil.isAjax(request)) {
      builder.entity(WebExceptionDTO.wrap(exception));
    }
    
    return builder.build();
  }
}
