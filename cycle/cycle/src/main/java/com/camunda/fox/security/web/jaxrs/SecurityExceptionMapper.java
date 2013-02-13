package com.camunda.fox.security.web.jaxrs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.camunda.fox.cycle.web.dto.WebExceptionDTO;
import com.camunda.fox.cycle.web.jaxrs.ext.JaxRsUtil;
import com.camunda.fox.security.MissingPrivilegesException;
import com.camunda.fox.security.SecurityException;
import com.camunda.fox.security.UnauthorizedException;
import com.camunda.fox.security.web.util.WebUtil;

/**
 *
 * @author nico.rehwaldt
 */
@Provider
public class SecurityExceptionMapper implements ExceptionMapper<com.camunda.fox.security.SecurityException> {

  @Context
  private HttpServletRequest request;

  @Override
  public Response toResponse(SecurityException exception) {
    Status status = Status.NOT_FOUND;
    String errorPage = null;
    
    if (exception instanceof MissingPrivilegesException) {
      status = Status.FORBIDDEN;
      errorPage = "tpl:error/forbidden";
    } else if (exception instanceof UnauthorizedException) {
      errorPage = "tpl:error/forbidden";
      status = Status.UNAUTHORIZED;
    }
    
    Response.ResponseBuilder builder = JaxRsUtil.createResponse().status(status);
    if (WebUtil.isAjax(request)) {
      builder.entity(WebExceptionDTO.wrap(exception, status));
    } else {
      builder.entity(errorPage);
    }

    return builder.build();
  }
}
