/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Collections;
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

  public static List<SecurityFilterRule> load(InputStream configFileResource,
                                              String applicationPath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    SecurityFilterConfig config = objectMapper.readValue(configFileResource, SecurityFilterConfig.class);
    return createFilterRules(config, applicationPath);
  }

  public static List<SecurityFilterRule> createFilterRules(SecurityFilterConfig config,
                                                           String applicationPath) {
    PathFilterConfig pathFilter = config.getPathFilter();
    PathFilterRule rule = createPathFilterRule(pathFilter, applicationPath);

    return new ArrayList<>(Collections.singletonList(rule));
  }

  protected static PathFilterRule createPathFilterRule(PathFilterConfig pathFilter,
                                                       String applicationPath) {
    PathFilterRule pathFilterRule = new PathFilterRule();

    for (PathMatcherConfig pathMatcherConfig : pathFilter.getDeniedPaths()) {
      pathFilterRule.getDeniedPaths().add(transformPathMatcher(pathMatcherConfig, applicationPath));
    }

    for (PathMatcherConfig pathMatcherConfig : pathFilter.getAllowedPaths()) {
      pathFilterRule.getAllowedPaths().add(transformPathMatcher(pathMatcherConfig, applicationPath));
    }

    return pathFilterRule;
  }

  protected static RequestMatcher transformPathMatcher(PathMatcherConfig pathMatcherConfig,
                                                       String applicationPath) {
    RequestFilter requestMatcher = new RequestFilter(
        pathMatcherConfig.getPath(),
        applicationPath,
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
