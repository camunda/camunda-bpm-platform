/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.auth;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.webapp.impl.security.SecurityActions;
import org.camunda.bpm.webapp.impl.security.SecurityActions.SecurityAction;


/**
 * <p>Servlet {@link Filter} implementation responsible for poulating the
 * {@link Authentications#getCurrent()} thread-local (ie. binding the current
 * set of authentications to the current thread so that it may esily be obtained 
 * by application parts not having access to the current session.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class AuthenticationFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
    
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    
    final HttpServletRequest req = (HttpServletRequest) request;
    final StatusAwareServletResponse resp = new StatusAwareServletResponse((HttpServletResponse) response);
    
    // get authentication from session
    Authentications authentications = Authentications.getFromSession(req.getSession());      
    Authentications.setCurrent(authentications);
    
    try {
      
      SecurityActions.runWithAuthentications(new SecurityAction<Void>() {
        public Void execute() {
          try {            
            chain.doFilter(request, response);            
          } catch(Exception e) {
            throw new RuntimeException(e);
          }
          return null;
        }
      }, authentications);
    } finally {      
      if(resp.getStatus() == 401) {
        // update cookie
        AuthenticationCookie.updateCookie(resp, req.getSession());
      }
      Authentications.clearCurrent();
      Authentications.updateSession(req.getSession(), authentications);
    }
    
  }

  protected void setProcessEngineAuthentications(Authentications authentications) {    
    for (Authentication authentication : authentications.getAuthentications()) {
      ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
      if(processEngine != null) {
        List<String> groupIds = null;
        String identityId = authentication.getIdentityId();
        if(authentication instanceof UserAuthentication) {
          groupIds = ((UserAuthentication) authentication).getGroupIds();
        }
        processEngine.getIdentityService().setAuthentication(identityId, groupIds);      
      }            
    }
  }
  
  protected void clearProcessEngineAuthentications(Authentications authentications) {    
    for (Authentication authentication : authentications.getAuthentications()) {
      ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
      if(processEngine != null) {
        processEngine.getIdentityService().clearAuthentication();      
      }            
    }
  }

  public void destroy() {
    
  }

}
