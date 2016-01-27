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
package org.camunda.bpm.webapp.impl.security.filter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.filter.Authorization;
import org.camunda.bpm.webapp.impl.security.filter.PathFilterRule;
import org.camunda.bpm.webapp.impl.security.filter.RequestAuthorizer;
import org.camunda.bpm.webapp.impl.security.filter.RequestFilter;
import org.camunda.bpm.webapp.impl.security.filter.RequestMatcher;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathFilterConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathMatcherConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterRule;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility to load and match filter rules.
 *
 * @author nico.rehwaldt
 */
public class FilterRules {

  public static List<SecurityFilterRule> load(InputStream configFileResource) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    SecurityFilterConfig config = objectMapper.readValue(configFileResource, SecurityFilterConfig.class);
    return createFilterRules(config);
  }

  public static List<SecurityFilterRule> createFilterRules(SecurityFilterConfig config) {
    PathFilterConfig pathFilter = config.getPathFilter();
    PathFilterRule rule = createPathFilterRule(pathFilter);

    return new ArrayList<SecurityFilterRule>(Arrays.asList(rule));
  }

  protected static PathFilterRule createPathFilterRule(PathFilterConfig pathFilter) {
    PathFilterRule pathFilterRule = new PathFilterRule();

    for (PathMatcherConfig pathMatcherConfig : pathFilter.getDeniedPaths()) {
      pathFilterRule.getDeniedPaths().add(transformPathMatcher(pathMatcherConfig));
    }

    for (PathMatcherConfig pathMatcherConfig : pathFilter.getAllowedPaths()) {
      pathFilterRule.getAllowedPaths().add(transformPathMatcher(pathMatcherConfig));
    }

    return pathFilterRule;
  }

  protected static RequestMatcher transformPathMatcher(PathMatcherConfig pathMatcherConfig) {
    RequestFilter requestMatcher = new RequestFilter(
        pathMatcherConfig.getPath(),
        pathMatcherConfig.getParsedMethods());

    RequestAuthorizer requestAuthorizer = RequestAuthorizer.AUTHORIZE_ANNONYMOUS;

    if (pathMatcherConfig.getAuthorizer() != null) {
      String authorizeCls = pathMatcherConfig.getAuthorizer();
      requestAuthorizer = (RequestAuthorizer) ReflectUtil.instantiate(authorizeCls);
    }

    return new RequestMatcher(requestMatcher, requestAuthorizer);
  }

  /**
   * Iterate over a number of filter rules and match them against
   * the given request.
   *
   * @param requestMethod
   * @param requestUri
   * @param filterRules
   *
   * @return the checked request with authorization information attached
   */
  public static Authorization authorize(String requestMethod, String requestUri, List<SecurityFilterRule> filterRules) {

    Authorization authorization;

    for (SecurityFilterRule filterRule : filterRules) {
      authorization = filterRule.authorize(requestMethod, requestUri);

      if (authorization != null) {
        return authorization;
      }
    }

    // grant if no filter disallows it
    return Authorization.granted(Authentication.ANONYMOUS);
  }
}
