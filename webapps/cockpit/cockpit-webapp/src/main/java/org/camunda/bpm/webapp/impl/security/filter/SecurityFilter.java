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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathFilterConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathMatcherConfig;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * <p>Simple filter implementation which delegates to a list of {@link SecurityFilterRule FilterRules}, 
 * evaluating their {@link SecurityFilterRule#isRequestAuthorized(HttpServletRequest)} condition 
 * for the given request. Each rule may veto the request by returing 'false'. If the request is 
 * vetoed, a 401 (unauthorized) response is returned.</p>
 * 
 * <p>This filter must be configured using a init-param in the web.xml file. The parameter must be named
 * "configFile" and point to the configuration file located in the servlet context.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class SecurityFilter implements Filter {
  
  public List<SecurityFilterRule> filterRules = new ArrayList<SecurityFilterRule>();

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    for (SecurityFilterRule filterRule : filterRules) {
      if(!filterRule.isRequestAuthorized((HttpServletRequest) request)) {
        // send unauthorized response
        ((HttpServletResponse) response).sendError(401);
        // break
        return;
      } 
    }
    
    // if all rules evaluate to true, continue chain.
    chain.doFilter(request, response);
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    String configFileName = filterConfig.getInitParameter("configFile");
    InputStream configFileResource = filterConfig.getServletContext().getResourceAsStream(configFileName);
    if(configFileResource == null) {
      throw new ServletException("Could not read security filter config file '"+configFileName+"': no such resource in servlet context.");      
    } else {      
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        SecurityFilterConfig config = objectMapper.readValue(configFileResource, SecurityFilterConfig.class);
        createFilterRules(config);
        
      } catch(Exception e) {
        throw new ServletException("Exception while parsing '"+configFileName+"'", e);
        
      } finally {
        IoUtil.closeSilently(configFileResource);
        
      }
    }
  }

  protected void createFilterRules(SecurityFilterConfig config) {
    PathFilterConfig pathFilter = config.getPathFilter();
    filterRules.add(createPathFilterRule(pathFilter));
  }

  protected PathFilterRule createPathFilterRule(PathFilterConfig pathFilter) {
    PathFilterRule pathFilterRule = new PathFilterRule();
    
    for (PathMatcherConfig pathMatcherConfig : pathFilter.getDeniedPaths()) {
      pathFilterRule.getDeniedPaths().add(transformPathMatcher(pathMatcherConfig));
    }
    
    for (PathMatcherConfig pathMatcherConfig : pathFilter.getAllowedPaths()) {
      pathFilterRule.getAllowedPaths().add(transformPathMatcher(pathMatcherConfig));
    } 
    
    return pathFilterRule;
  }

  protected RequestMatcher transformPathMatcher(PathMatcherConfig pathMatcherConfig) {
    RequestMatcher requestMatcher = null;
    if(pathMatcherConfig.getMatcher() == null || pathMatcherConfig.getMatcher().isEmpty()) {
      requestMatcher = new RequestMatcher(pathMatcherConfig.getPath(), pathMatcherConfig.getParsedMethods());
    } else {
      String matcher = pathMatcherConfig.getMatcher();
      Object[] params = new Object[]{pathMatcherConfig.getPath(), pathMatcherConfig.getParsedMethods()};
      requestMatcher = (RequestMatcher) ReflectUtil.instantiate(matcher, params);
    }
    return requestMatcher;
  }

  public void destroy() {

  }

}
