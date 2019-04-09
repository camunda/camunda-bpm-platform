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
package org.camunda.bpm.engine.test.api.authorization.util;


/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationScenario {

  protected static final String INDENTATION = "   ";

  protected AuthorizationSpec[] givenAuthorizations = new AuthorizationSpec[]{};
  protected AuthorizationSpec[] missingAuthorizations = new AuthorizationSpec[]{};

  public static AuthorizationScenario scenario() {
    return new AuthorizationScenario();
  }

  public AuthorizationScenario withoutAuthorizations() {
    return this;
  }

  public AuthorizationScenario withAuthorizations(AuthorizationSpec... givenAuthorizations) {
    this.givenAuthorizations = givenAuthorizations;
    return this;
  }

  public AuthorizationScenario succeeds() {
    return this;
  }

  public AuthorizationScenario failsDueToRequired(AuthorizationSpec... expectedMissingAuthorizations) {
    this.missingAuthorizations = expectedMissingAuthorizations;
    return this;
  }

  public AuthorizationSpec[] getGivenAuthorizations() {
    return givenAuthorizations;
  }

  public AuthorizationSpec[] getMissingAuthorizations() {
    return missingAuthorizations;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Given Authorizations: \n");
    for (AuthorizationSpec spec : givenAuthorizations) {
      sb.append(INDENTATION);
      sb.append(spec);
      sb.append("\n");
    }

    sb.append("Expected missing authorizations: \n");
    for (AuthorizationSpec spec : missingAuthorizations) {
      sb.append(INDENTATION);
      sb.append(spec);
      sb.append("\n");
    }

    return sb.toString();
  }


}
