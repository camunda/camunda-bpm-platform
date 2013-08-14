package org.camunda.bpm.webapp.impl.security.filter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.webapp.impl.security.filter.PathFilterRule;
import org.camunda.bpm.webapp.impl.security.filter.RequestMatcher;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathFilterConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterConfig.PathMatcherConfig;
import org.camunda.bpm.webapp.impl.security.filter.SecurityFilterRule;
import org.codehaus.jackson.map.ObjectMapper;

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

  /**
   * Iterate over a number of filter rules and match them against
   * the specified request.
   *
   * @param request
   * @param filterRules
   *
   * @return true if the request is authorized against all filter rules, false otherwise
   */
  public static boolean isAuthorized(HttpServletRequest request, List<SecurityFilterRule> filterRules) {

    for (SecurityFilterRule filterRule : filterRules) {
      if (!filterRule.isRequestAuthorized((HttpServletRequest) request)) {
        return false;
      }
    }

    return true;
  }
}
