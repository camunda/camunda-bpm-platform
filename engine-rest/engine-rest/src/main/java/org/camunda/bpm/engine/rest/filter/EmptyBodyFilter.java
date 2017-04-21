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
package org.camunda.bpm.engine.rest.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * @author Tassilo Weidner
 */
public class EmptyBodyFilter implements Filter {

  protected static final Pattern CONTENT_TYPE_JSON_PATTERN = Pattern.compile("^application\\/json((;)(.*)?)?$", Pattern.CASE_INSENSITIVE);

  @Override
  public void doFilter(final ServletRequest REQ, final ServletResponse RESP, FilterChain chain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) REQ;

    boolean isContentTypeJSON = CONTENT_TYPE_JSON_PATTERN
      .matcher(request.getContentType() == null ? "" : request.getContentType())
      .find();

    if ("POST".equals(request.getMethod()) && isContentTypeJSON && request.getContentLength() == 0) {

      request = new HttpServletRequestWrapper(request) {

        @Override
        public ServletInputStream getInputStream() throws IOException {
          final ByteArrayInputStream BYTE_ARRAY_INPUT_STREAM = new ByteArrayInputStream("{}".getBytes());

          return new ServletInputStream() {
            public int read() throws IOException {
              return BYTE_ARRAY_INPUT_STREAM.read();
            }
          };
        }

        @Override
        public BufferedReader getReader() throws IOException {
          return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

      };

    }

    chain.doFilter(request, RESP);
  }

  @Override
  public void destroy() {

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

}
