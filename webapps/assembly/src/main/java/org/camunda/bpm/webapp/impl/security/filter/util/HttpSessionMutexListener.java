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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.Serializable;

/**
 * Automatically creates a session mutex when a HttpSession
 * is created. This way, it is guaranteed that the session mutex is
 * the same object throughout the session lifetime. This is not
 * 100% guaranteed across all possible servlet containers when using
 * the HttpSession reference itself.
 *
 * The session mutex can be accessed under the {@link CsrfConstants#CSRF_SESSION_MUTEX}
 * session attribute and the {@link HttpSessionMutexListener} needs
 * to be registered as a listener in {@code web.xml}.
 *
 * @author Nikola Koevski
 */
public class HttpSessionMutexListener implements HttpSessionListener {

  public static final String AUTH_TIME_SESSION_MUTEX = "CAMUNDA_AUTH_TIME_SESSION_MUTEX";

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    event.getSession().setAttribute(CsrfConstants.CSRF_SESSION_MUTEX, new SessionMutex());
    event.getSession().setAttribute(AUTH_TIME_SESSION_MUTEX, new SessionMutex());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    event.getSession().removeAttribute(CsrfConstants.CSRF_SESSION_MUTEX);
    event.getSession().removeAttribute(AUTH_TIME_SESSION_MUTEX);
  }

  /**
   * Just a class to instantiate serializable objects to
   * synchronize the session on. Needs to be serializable
   * to allow for HttpSession persistence.
   */
  private static class SessionMutex implements Serializable {
  }
}
