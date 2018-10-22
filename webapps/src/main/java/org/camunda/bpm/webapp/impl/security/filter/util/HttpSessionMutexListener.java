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

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    event.getSession().setAttribute(CsrfConstants.CSRF_SESSION_MUTEX, new SessionMutex());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    event.getSession().removeAttribute(CsrfConstants.CSRF_SESSION_MUTEX);
  }

  /**
   * Just a class to instantiate serializable objects to
   * synchronize the session on. Needs to be serializable
   * to allow for HttpSession persistence.
   */
  private static class SessionMutex implements Serializable {
  }
}
