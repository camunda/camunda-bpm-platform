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
package org.camunda.bpm.webapp.impl.security.filter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * <p>A {@link SecurityFilterRule} that deleagates to a set of Path Matchers</p>
 * 
 * @author Daniel Meyer
 *
 */
public class PathFilterRule implements SecurityFilterRule {

  protected List<RequestMatcher> deniedPaths = new ArrayList<RequestMatcher>();
  protected List<RequestMatcher> allowedPaths = new ArrayList<RequestMatcher>();
  
  public boolean isRequestAuthorized(HttpServletRequest req) {

    String contextPath = req.getContextPath();
    String requestUri = req.getRequestURI().substring(contextPath.length());
    
    boolean isRequestAuthorized = true;    
    for (RequestMatcher requestMatcher : deniedPaths) {
      if(requestMatcher.matches(req.getMethod(), requestUri)) {
        isRequestAuthorized = false;
        break;
      }
    }
    
    if(!isRequestAuthorized) {
      for (RequestMatcher requestMatcher : allowedPaths) {
        if(requestMatcher.matches(req.getMethod(), requestUri)) {
          return true;
        }
      }
    }
    
    return isRequestAuthorized;    
  }
  
  public List<RequestMatcher> getAllowedPaths() {
    return allowedPaths;
  }
  
  public List<RequestMatcher> getDeniedPaths() {
    return deniedPaths;
  }
  
}
