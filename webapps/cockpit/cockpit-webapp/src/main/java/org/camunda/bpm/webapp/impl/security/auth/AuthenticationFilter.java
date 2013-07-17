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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    final HttpServletRequest req = (HttpServletRequest) request;
    
    // get user from session
    Authentications authentications = Authentications.getFromSession(req.getSession());
      
    Authentications.setCurrent(authentications);
    try {
      // continue filter chain
      chain.doFilter(request, response);
      
    } finally {
      Authentications.clearCurrent();
      
    }
  }

  public void destroy() {
    
  }

}
