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
package org.camunda.bpm.webapp.impl.security.filter.headersec.provider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public abstract class HeaderSecurityProvider {

  protected boolean disabled = false;
  protected String value = null;
  protected Map<String, String> initParams = new HashMap<>();

  abstract public Map<String, String> initParams();

  abstract public void parseParams();

  abstract public String getHeaderName();

  public String getHeaderValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isDisabled() {
    return disabled;
  }

  protected void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  protected boolean isEmpty(String string) {
    return string == null || string.trim().isEmpty();
  }

}
