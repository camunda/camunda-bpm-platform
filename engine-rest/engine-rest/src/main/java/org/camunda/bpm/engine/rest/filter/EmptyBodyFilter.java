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
import java.io.PushbackInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author Tassilo Weidner
 */
public class EmptyBodyFilter implements Filter {

  protected static final Pattern CONTENT_TYPE_JSON_PATTERN = Pattern.compile("^application\\/json((;)(.*)?)?$", Pattern.CASE_INSENSITIVE);

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse resp, FilterChain chain) throws IOException, ServletException {

    final boolean isContentTypeJson =
      CONTENT_TYPE_JSON_PATTERN.matcher(req.getContentType() == null ? "" : req.getContentType()).find();

    if (isContentTypeJson) {
      final PushbackInputStream requestBody = new PushbackInputStream(req.getInputStream());
      int firstByte = requestBody.read();
      final boolean isBodyEmpty = firstByte == -1;
      requestBody.unread(firstByte);

      HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper((HttpServletRequest) req) {

        @Override
        public ServletInputStream getInputStream() throws IOException {

          return new ServletInputStream() {

            InputStream inputStream = isBodyEmpty ? new ByteArrayInputStream("{}".getBytes(Charset.forName("UTF-8"))) : requestBody;

            @Override
            public int read() throws IOException {
              return inputStream.read();
            }

            @Override
            public int available() throws IOException {
              return inputStream.available();
            }

            @Override
            public void close() throws IOException {
              inputStream.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
              inputStream.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
              inputStream.reset();
            }

            @Override
            public boolean markSupported() {
              return inputStream.markSupported();
            }

          };
        }

        @Override
        public BufferedReader getReader() throws IOException {
          return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

      };

      chain.doFilter(wrappedRequest, resp);
    } else {
      chain.doFilter(req, resp);
    }
  }

  @Override
  public void destroy() {

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

}
