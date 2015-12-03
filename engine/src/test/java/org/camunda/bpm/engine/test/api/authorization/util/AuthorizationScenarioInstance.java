/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization.util;

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuthorizationScenarioInstance {

  protected AuthorizationScenario scenario;

  protected List<Authorization> createdAuthorizations = new ArrayList<Authorization>();
  protected List<Authorization> missingAuthorizations = new ArrayList<Authorization>();

  public AuthorizationScenarioInstance(AuthorizationScenario scenario, AuthorizationService authorizationService,
      Map<String, String> resourceBindings) {
    this.scenario = scenario;
    init(authorizationService, resourceBindings);
  }

  public void init(AuthorizationService authorizationService, Map<String, String> resourceBindings) {
    for (AuthorizationSpec authorizationSpec : scenario.getGivenAuthorizations()) {
      Authorization authorization = authorizationSpec.instantiate(authorizationService, resourceBindings);
      authorizationService.saveAuthorization(authorization);
      createdAuthorizations.add(authorization);
    }

    for (AuthorizationSpec authorizationSpec : scenario.getMissingAuthorizations()) {
      Authorization authorization = authorizationSpec.instantiate(authorizationService, resourceBindings);
      missingAuthorizations.add(authorization);
    }
  }

  public void tearDown(AuthorizationService authorizationService) {
    Set<String> activeAuthorizations = new HashSet<String>();
    for (Authorization activeAuthorization : authorizationService.createAuthorizationQuery().list()) {
      activeAuthorizations.add(activeAuthorization.getId());
    }

    for (Authorization createdAuthorization : createdAuthorizations) {
      if (activeAuthorizations.contains(createdAuthorization.getId())) {
        authorizationService.deleteAuthorization(createdAuthorization.getId());
      }
    }
  }

  public void assertAuthorizationException(AuthorizationException e) {
    if (!missingAuthorizations.isEmpty() && e != null) {
      String message = e.getMessage();
      String assertionFailureMessage = describeScenarioFailure("Expected an authorization exception but the message was wrong: " + e.getMessage());

      List<MissingAuthorization> actualMissingAuthorizations = new ArrayList<MissingAuthorization>(e.getMissingAuthorizations());
      List<MissingAuthorization> expectedMissingAuthorizations = MissingAuthorizationMatcher.asMissingAuthorizations(missingAuthorizations);

      Assert.assertThat(actualMissingAuthorizations, containsInAnyOrder(MissingAuthorizationMatcher.asMatchers(expectedMissingAuthorizations)));

      for (Authorization missingAuthorization : missingAuthorizations) {
        Assert.assertTrue(assertionFailureMessage, message.contains(missingAuthorization.getUserId()));
        Assert.assertEquals(missingAuthorization.getUserId(), e.getUserId());

        for (Permission permission : missingAuthorization.getPermissions(Permissions.values())) {
          if (permission != Permissions.NONE) {
            Assert.assertTrue(assertionFailureMessage, message.contains(permission.getName()));
          }
        }

        if (!Authorization.ANY.equals(missingAuthorization.getResourceId())) {
          // missing ANY authorizations are not explicitly represented in the error message
          Assert.assertTrue(assertionFailureMessage, message.contains(missingAuthorization.getResourceId()));
        }

        Resource resource = AuthorizationTestUtil.getResourceByType(missingAuthorization.getResourceType());
        Assert.assertTrue(assertionFailureMessage, message.contains(resource.resourceName()));
      }
    }
    else if (missingAuthorizations.isEmpty() && e == null) {
      // nothing to do
    }
    else {
      if (e != null) {
        Assert.fail(describeScenarioFailure("Expected no authorization exception but got one: " + e.getMessage()));
      }
      else {
        Assert.fail(describeScenarioFailure("Expected failure due to missing authorizations but code under test was successful"));
      }
    }
  }

  protected String describeScenarioFailure(String message) {
    return message + "\n"
        + "\n"
        + "Scenario: \n"
        + scenario.toString();
  }
}
