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
package org.camunda.bpm.webapp.impl.security.filter.headersec;

import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentSecurityPolicyProvider;
import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.ContentTypeOptionsProvider;
import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.HeaderSecurityProvider;
import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.StrictTransportSecurityProvider;
import org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl.XssProtectionProvider;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class HttpHeaderSecurityFilter implements Filter {

  protected final List<HeaderSecurityProvider> headerSecurityProviders = new ArrayList<HeaderSecurityProvider>() {{

    add(new XssProtectionProvider());
    add(new ContentSecurityPolicyProvider());
    add(new ContentTypeOptionsProvider());
    add(new StrictTransportSecurityProvider());

  }};

  public void init(FilterConfig filterConfig) {

    for (HeaderSecurityProvider provider : headerSecurityProviders) {

      Map<String, String> filterParams = provider.initParams();

      for (Map.Entry<String, String> filterParam : filterParams.entrySet()) {

        String key = filterParam.getKey();
        String value = filterConfig.getInitParameter(key);

        if (value != null) {
          filterParam.setValue(value);
        }
      }

      provider.parseParams();
    }
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    if (response instanceof HttpServletResponse) {

      HttpServletResponse httpResponse = (HttpServletResponse) response;

      for (HeaderSecurityProvider provider : headerSecurityProviders) {

        if (!provider.isDisabled()) {

          String headerName = provider.getHeaderName();
          String headerValue = provider.getHeaderValue(request.getServletContext());

          httpResponse.setHeader(headerName, headerValue);
        }
      }
    }

    chain.doFilter(request, response);
  }

  public void destroy() {
  }

}
