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
package org.camunda.bpm.engine.rest.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.InputStream;

/**
 * @author Tassilo Weidner
 */
public class EmptyBodyFilter extends AbstractEmptyBodyFilter {

  @Override
  public HttpServletRequestWrapper wrapRequest(HttpServletRequest req, boolean isBodyEmpty, PushbackInputStream requestBody) {
    return new HttpServletRequestWrapper(req) {

      @Override
      public ServletInputStream getInputStream() throws IOException {

        return new ServletInputStream() {

          final InputStream inputStream = getRequestBody(isBodyEmpty, requestBody);

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
        return EmptyBodyFilter.this.getReader(this.getInputStream());
      }

    };
  }

}
