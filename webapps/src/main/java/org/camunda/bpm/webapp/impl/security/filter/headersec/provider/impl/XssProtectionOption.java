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
package org.camunda.bpm.webapp.impl.security.filter.headersec.provider.impl;

/**
 * @author Tassilo Weidner
 */
public enum XssProtectionOption {

  BLOCK("1; mode=block"),
  SANITIZE("1");

  protected final String headerValue;

  XssProtectionOption(String headerValue) {
    this.headerValue = headerValue;
  }

  public String getHeaderValue() {
    return headerValue;
  }

  public String getName() {
    return this.name();
  }

}
