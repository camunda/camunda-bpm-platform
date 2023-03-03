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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author Tassilo Weidner
 */
public abstract class AbstractEmptyBodyFilter implements Filter {

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

      chain.doFilter(wrapRequest((HttpServletRequest) req, isBodyEmpty, requestBody), resp);
    } else {
      chain.doFilter(req, resp);
    }
  }

  public InputStream getRequestBody(boolean isBodyEmpty, PushbackInputStream requestBody) {
    return isBodyEmpty ? new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)) : requestBody;
  }

  public abstract HttpServletRequestWrapper wrapRequest(HttpServletRequest req, boolean isBodyEmpty, PushbackInputStream requestBody);

  @Override
  public void destroy() {

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  public BufferedReader getReader(final ServletInputStream inputStream) {
    return new BufferedReader(new InputStreamReader(inputStream));
  }
}
