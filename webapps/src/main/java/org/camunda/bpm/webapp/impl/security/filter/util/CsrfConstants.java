package org.camunda.bpm.webapp.impl.security.filter.util;

import java.util.regex.Pattern;

/**
 * @author Nikola Koevski
 */
public final class CsrfConstants {

  public static final String CSRF_TOKEN_SESSION_ATTR_NAME = "CAMUNDA_CSRF_TOKEN";

  public static final String CSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";

  public static final String CSRF_TOKEN_HEADER_REQUIRED = "Required";

  public static final String CSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

  public static final Pattern CSRF_NON_MODIFYING_METHODS_PATTERN = Pattern.compile("GET|HEAD|OPTIONS");

  public static final Pattern CSRF_DEFAULT_ENTRY_URL_PATTERN = Pattern.compile("^/api/admin/auth/user/.+/login/(cockpit|tasklist|admin|welcome)$");

}
