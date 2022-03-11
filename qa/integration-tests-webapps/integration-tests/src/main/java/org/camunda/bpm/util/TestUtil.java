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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.TestProperties;
import org.camunda.bpm.engine.rest.dto.identity.UserCredentialsDto;
import org.camunda.bpm.engine.rest.dto.identity.UserDto;
import org.camunda.bpm.engine.rest.dto.identity.UserProfileDto;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 *
 * @author nico.rehwaldt
 */
public class TestUtil {

  private final ApacheHttpClient4 client;

  private final TestProperties testProperties;

  public TestUtil(TestProperties testProperties) {

    this.testProperties = testProperties;

    // create admin user:
    ClientConfig clientConfig = new DefaultApacheHttpClient4Config();
    clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
    client = ApacheHttpClient4.create(clientConfig);

  }

  public void destroy() {
    client.destroy();
  }

  public void createInitialUser(String id, String password, String firstName, String lastName) {

    UserDto user = new UserDto();
    UserCredentialsDto credentials = new UserCredentialsDto();
    credentials.setPassword(password);
    user.setCredentials(credentials);
    UserProfileDto profile = new UserProfileDto();
    profile.setId(id);
    profile.setFirstName(firstName);
    profile.setLastName(lastName);
    user.setProfile(profile);

    WebResource webResource = client.resource(testProperties.getApplicationPath("/camunda/api/admin/setup/default/user/create"));
    ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
    try {
      if (clientResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
        throw new WebApplicationException(clientResponse.getStatus());
      }
    } finally {
      clientResponse.close();
    }
  }
  
  public void deleteUser(String id) {
    // delete admin user
    WebResource webResource = client.resource(testProperties.getApplicationPath("/engine-rest/user/admin"));
    webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).delete();
  }
}
