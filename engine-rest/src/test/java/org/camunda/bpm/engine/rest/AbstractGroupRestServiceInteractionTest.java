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
package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.rest.dto.identity.GroupDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractGroupRestServiceInteractionTest extends AbstractRestServiceTest {
  
  protected static final String GROUP_URL = TEST_RESOURCE_ROOT_PATH + "/group/{id}";
  protected static final String CREATE_GROUP_URL = TEST_RESOURCE_ROOT_PATH + "/group/create";
  
  protected IdentityService identityServiceMock;
  
  @Before
  public void setupGroupData() {
    
    identityServiceMock = mock(IdentityService.class);
    
    // mock identity service
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);
    
  }
  
  @Test
  public void testGetSingleGroup() {    
    Group sampleGroup = MockProvider.createMockGroup();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(sampleGroup);
    
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .body("id", equalTo(MockProvider.EXAMPLE_GROUP_ID))
      .body("name", equalTo(MockProvider.EXAMPLE_GROUP_NAME))
      .when().get(GROUP_URL);
  }
  
  @Test
  public void testGetNonExistingGroup() {    
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingGroup")
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Group with id aNonExistingGroup does not exist"))
      .when().get(GROUP_URL);
  }
  
  @Test
  public void testDeleteGroup() {    
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .expect().statusCode(Status.NO_CONTENT.getStatusCode())      
      .when().delete(GROUP_URL);
  }
  
  @Test
  public void testDeleteNonExistingGroup() {    
    given().pathParam("id", "non-existing")
    .expect().statusCode(Status.NO_CONTENT.getStatusCode())      
    .when().delete(GROUP_URL);    
  }
  
  @Test
  public void testUpdateExistingGroup() {    
    Group initialGroup = MockProvider.createMockGroup();
    Group groupUpdate = MockProvider.createMockGroupUpdate();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.singleResult()).thenReturn(initialGroup);
    
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .body(GroupDto.fromGroup(groupUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())      
      .when().post(GROUP_URL);
    
    // initial group was updated
    verify(initialGroup).setName(groupUpdate.getName());
    
    // and then saved
    verify(identityServiceMock).saveGroup(initialGroup);
  }
  
  @Test
  public void testUpdateNonExistingGroup() {
    Group groupUpdate = MockProvider.createMockGroupUpdate();
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    when(identityServiceMock.createGroupQuery()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.groupId("aNonExistingGroup")).thenReturn(sampleGroupQuery);    
    // this time the query returns null
    when(sampleGroupQuery.singleResult()).thenReturn(null);
    
    given().pathParam("id", "aNonExistingGroup")
      .body(GroupDto.fromGroup(groupUpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.NOT_FOUND.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Group with id aNonExistingGroup does not exist"))
      .when().post(GROUP_URL);
    
    verify(identityServiceMock, never()).saveGroup(any(Group.class));    
  }

  @Test
  public void testGroupCreate() {
    Group newGroup = MockProvider.createMockGroup();    
    when(identityServiceMock.newGroup(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(newGroup);
    
    given().body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.OK.getStatusCode()).contentType(ContentType.JSON)
      .when().post(CREATE_GROUP_URL);
    
    verify(identityServiceMock).newGroup(MockProvider.EXAMPLE_GROUP_ID);
    verify(newGroup).setName(MockProvider.EXAMPLE_GROUP_NAME);
    verify(identityServiceMock).saveGroup(newGroup);
  }
  
  @Test
  public void testGroupCreateExistingFails() {
    Group newGroup = MockProvider.createMockGroup();    
    when(identityServiceMock.newGroup(MockProvider.EXAMPLE_GROUP_ID)).thenReturn(newGroup);
    doThrow(new RuntimeException("")).when(identityServiceMock).saveGroup(newGroup);
    
    given().body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Exception while saving new group "))
      .when().post(CREATE_GROUP_URL);
    
    verify(identityServiceMock).newGroup(MockProvider.EXAMPLE_GROUP_ID);
    verify(identityServiceMock).saveGroup(newGroup);
  }
  
  @Test
  public void testReadOnlyGroupCreateFails() {
    Group newGroup = MockProvider.createMockGroup();    
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().body(GroupDto.fromGroup(newGroup)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().post(CREATE_GROUP_URL);
    
    verify(identityServiceMock, never()).newGroup(MockProvider.EXAMPLE_GROUP_ID);    
  }
  
  @Test
  public void testReadOnlyGroupUpdateFails() {
    Group groupUdpdate = MockProvider.createMockGroup();    
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
       .body(GroupDto.fromGroup(groupUdpdate)).contentType(ContentType.JSON)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().post(GROUP_URL);
    
    verify(identityServiceMock, never()).saveGroup(groupUdpdate);    
  }
  
  @Test
  public void testReadOnlyGroupDeleteFails() {
    when(identityServiceMock.isReadOnly()).thenReturn(true);
    
    given().pathParam("id", MockProvider.EXAMPLE_GROUP_ID)
      .then().expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", equalTo("Identity service implementation is read-only."))
      .when().delete(GROUP_URL);
    
    verify(identityServiceMock, never()).deleteGroup(MockProvider.EXAMPLE_GROUP_ID);    
  }

}
