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

import java.util.regex.Pattern;

/**
 * @author Nikola Koevski
 */
public final class CsrfConstants {

  public static final String CSRF_SESSION_MUTEX = "CAMUNDA_CSRF_SESSION_MUTEX";

  public static final String CSRF_TOKEN_SESSION_ATTR_NAME = "CAMUNDA_CSRF_TOKEN";

  public static final String CSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";

  public static final String CSRF_TOKEN_HEADER_REQUIRED = "Required";

  public static final String CSRF_TOKEN_DEFAULT_COOKIE_NAME = "XSRF-TOKEN";

  public static final Pattern CSRF_NON_MODIFYING_METHODS_PATTERN = Pattern.compile("GET|HEAD|OPTIONS");

  public static final String CSRF_PATH_FIELD_NAME = ";Path=";

}
