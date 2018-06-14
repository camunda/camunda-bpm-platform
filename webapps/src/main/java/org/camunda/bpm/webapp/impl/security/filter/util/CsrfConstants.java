package org.camunda.bpm.webapp.impl.security.filter.util;

/**
 * @author Nikola Koevski
 */
public final class CsrfConstants {

  public static final String CSRF_TOKEN_SESSION_ATTR_NAME = "CAMUNDA_CSRF_NONCE";

  public static final String CSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";

  public static final String CSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

}
