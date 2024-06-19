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
package org.camunda.bpm.util;

import org.camunda.bpm.TestProperties;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.JsonNode;

/**
 *
 * @author nico.rehwaldt
 */
public class TestUtil {

  private final TestProperties testProperties;

  public TestUtil(TestProperties testProperties) {
    this.testProperties = testProperties;
  }

  public void createUser(String userId, String password, String firstName, String lastName, String email) {
    UserDto user = new UserDto();
    UserProfileDto profile = new UserProfileDto();
    profile.setId(userId);
    profile.setFirstName(firstName);
    profile.setLastName(lastName);
    profile.setEmail(email);
    user.setProfile(profile);

    UserCredentialsDto credentials = new UserCredentialsDto();
    credentials.setPassword(password);
    user.setCredentials(credentials);

    HttpResponse<String> response = Unirest.post(testProperties.getApplicationPath("/camunda/api/admin/setup/default/user/create"))
        .header("Content-Type", "application/json")
        .body(user)
        .asString();

    if (response.getStatus() != 204) {
      throw new RuntimeException("Unable to create user: " + response.getStatus());
    }
  }

  public void deleteUser(String userId) {
    HttpResponse<String> response = Unirest.delete(testProperties.getApplicationPath("/engine-rest/user/" + userId))
        .asString();

    if (response.getStatus() != 204) {
      throw new RuntimeException("Unable to delete user: " + response.getStatus());
    }
  }

  public void destroy() {
    // Cleanup method for compatibility
    // No specific cleanup needed for this utility class
  }
}
