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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.util.Random;


/**
 * This code was ported from the <code>CsrfPreventionFilterBase</code> class
 * of Apache Tomcat.
 *
 * @author Nikola Koevski
 */
public abstract class BaseCsrfPreventionFilter implements Filter {

  private String randomClass = SecureRandom.class.getName();

  private Random randomSource;

  private int denyStatus = HttpServletResponse.SC_FORBIDDEN;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    try {

      String customDenyStatus = filterConfig.getInitParameter("denyStatus");
      if (!isBlank(customDenyStatus)) {
        setDenyStatus(Integer.valueOf(customDenyStatus));
      }

      String newRandomClass = filterConfig.getInitParameter("randomClass");
      if (!isBlank(newRandomClass)) {
        setRandomClass(newRandomClass);
      }

      Class<?> clazz = Class.forName(randomClass);
      randomSource = (Random) clazz.getConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: Random class not found.", e);
    } catch (InstantiationException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (InvocationTargetException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (NoSuchMethodException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: cannot instantiate provided Random class", e);
    } catch (IllegalAccessException e) {
      throw new ServletException("Cannot instantiate CSRF Prevention filter: Random class constructor not accessible", e);
    }
  }

  /**
   * @return the response status code that is used to reject a denied request.
   */
  public int getDenyStatus() {
    return denyStatus;
  }

  /**
   * Sets the response status code that is used to reject denied request.
   * If none is set, the default value of 403 will be used.
   *
   * @param denyStatus
   *            HTTP status code
   */
  public void setDenyStatus(int denyStatus) {
    this.denyStatus = denyStatus;
  }

  /**
   * Specify the class to use to generate the tokens. Must be an instance of
   * {@link Random}.
   *
   * @param randomClass
   *            The name of the class to use
   */
  public void setRandomClass(String randomClass) {
    this.randomClass = randomClass;
  }

  /**
   * Generate a once time token for authenticating subsequent
   * requests.
   *
   * @return the generated token
   */
  protected String generateToken() {
    byte random[] = new byte[16];

    // Render the result as a String of hexadecimal digits
    StringBuilder buffer = new StringBuilder();

    randomSource.nextBytes(random);

    for (int j = 0; j < random.length; j++) {
      byte b1 = (byte) ((random[j] & 0xf0) >> 4);
      byte b2 = (byte) (random[j] & 0x0f);
      if (b1 < 10) {
        buffer.append((char) ('0' + b1));
      } else {
        buffer.append((char) ('A' + (b1 - 10)));
      }
      if (b2 < 10) {
        buffer.append((char) ('0' + b2));
      } else {
        buffer.append((char) ('A' + (b2 - 10)));
      }
    }

    return buffer.toString();
  }

  protected boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  @Override
  public void destroy() {
  }
}
