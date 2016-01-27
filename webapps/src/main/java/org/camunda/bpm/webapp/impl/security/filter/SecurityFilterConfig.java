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

/**
 * <p>POJO representing the configuration of the security filter</p>
 *
 * <p>An instance of this file is deserialized fron JSON config by the
 * {@link SecurityFilter} (see
 * {@link SecurityFilter#init(javax.servlet.FilterConfig)}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class SecurityFilterConfig {

  protected PathFilterConfig pathFilter;

  public PathFilterConfig getPathFilter() {
    return pathFilter;
  }

  public void setPathFilter(PathFilterConfig pathFilter) {
    this.pathFilter = pathFilter;
  }

  public static class PathFilterConfig {

    protected List<PathMatcherConfig> deniedPaths = new ArrayList<PathMatcherConfig>();
    protected List<PathMatcherConfig> allowedPaths = new ArrayList<PathMatcherConfig>();

    public List<PathMatcherConfig> getDeniedPaths() {
      return deniedPaths;
    }

    public void setDeniedPaths(List<PathMatcherConfig> protectedPaths) {
      this.deniedPaths = protectedPaths;
    }

    public List<PathMatcherConfig> getAllowedPaths() {
      return allowedPaths;
    }

    public void setAllowedPaths(List<PathMatcherConfig> allowedPaths) {
      this.allowedPaths = allowedPaths;
    }

  }

  public static class PathMatcherConfig {

    protected String path;
    protected String methods;
    protected String authorizer;

    public String getPath() {
      return path;
    }
    public void setPath(String path) {
      this.path = path;
    }
    public String getMethods() {
      return methods;
    }
    public void setMethods(String methods) {
      this.methods = methods;
    }
    public String getAuthorizer() {
      return authorizer;
    }
    public void setAuthorizer(String authorizer) {
      this.authorizer = authorizer;
    }
    public String[] getParsedMethods() {
      if(methods == null || methods.isEmpty() || methods.equals("*") || methods.toUpperCase().equals("ALL")) {
        return new String[0];
      } else {
        return methods.split(",");
      }
    }

  }

}
