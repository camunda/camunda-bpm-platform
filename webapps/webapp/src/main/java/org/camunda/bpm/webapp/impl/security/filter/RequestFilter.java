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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A request matcher that matches uris compatible to the JAX-RS syntax
 * and extracts the arguments on match.</p>
 *
 * Example uris:
 *
 * <code>/some/url/{param1}/{param2}/{param3:.*}</code>
 * <code>/some/url/{param1:foo}/.*</code>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class RequestFilter {

  protected String[] methods;
  protected Pattern pattern;

  private String[] groups;

  public RequestFilter(String pattern, String... methods) {
    this.methods = methods;

    setPattern(pattern);
  }

  public Map<String, String> match(String requestMethod, String requestUri) {

    if (!isMethodMatched(requestMethod)) {
      return null;
    }

    return matchRequestUri(requestUri);
  }

  protected boolean isMethodMatched(String requestMethod) {
    boolean isMethodMatched = false;
    if (methods.length != 0) {
      for (String method : methods) {
        if (method.equals(requestMethod)) {
          isMethodMatched = true;
          break;
        }
      }
    } else {
      isMethodMatched = true;
    }
    return isMethodMatched;
  }

  protected Map<String, String> matchRequestUri(String requestUri) {
    Matcher matcher = pattern.matcher(requestUri);

    if (!matcher.matches()) {
      return null;
    }

    HashMap<String, String> attributes = new HashMap<String, String>();

    for (int i = 0; i < matcher.groupCount(); i++) {
      attributes.put(groups[i], matcher.group(i + 1));
    }

    return attributes;
  }

  /**
   * Sets the uri pattern for this matcher
   * @param pattern
   */
  protected final void setPattern(String pattern) {

    String[] parts = pattern.split("/");

    ArrayList<String> groupList = new ArrayList<String>();

    StringBuilder regexBuilder = new StringBuilder();
    boolean first = true;

    for (String part: parts) {
      String group = null;
      String regex = part;

      // parse group
      if (part.startsWith("{") && part.endsWith("}")) {
        String groupStr = part.substring(1, part.length() - 1);

        String[] groupSplit = groupStr.split(":");
        if (groupSplit.length > 2) {
          throw new IllegalArgumentException("cannot parse uri part " + regex + " in " + pattern + ": expected {asdf(:pattern)}");
        }

        group = groupSplit[0];
        if (groupSplit.length > 1) {
          regex = "(" + groupSplit[1] + ")";
        } else {
          regex = "([^/]+)";
        }
      }

      if (!first) {
        regexBuilder.append("/");
      } else {
        first = false;
      }

      regexBuilder.append(regex);

      if (group != null) {
        groupList.add(group);
      }
    }

    this.groups = groupList.toArray(new String[0]);
    this.pattern = Pattern.compile(regexBuilder.toString());
  }
}