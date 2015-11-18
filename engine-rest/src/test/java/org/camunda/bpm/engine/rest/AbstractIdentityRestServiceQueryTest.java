package org.camunda.bpm.engine.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

public abstract class AbstractIdentityRestServiceQueryTest extends AbstractRestServiceTest {

  protected static final String IDENTITY_URL = TEST_RESOURCE_ROOT_PATH + "/identity";
  protected static final String TASK_GROUPS_URL = IDENTITY_URL + "/groups";
  
  private User mockUser;
  
  @Before
  public void setUpRuntimeData() {
    createMockIdentityQueries();
  }
  
  private void createMockIdentityQueries() {
    UserQuery sampleUserQuery = mock(UserQuery.class);
    
    List<User> mockUsers = new ArrayList<User>();
    
    mockUser = MockProvider.createMockUser();
    mockUsers.add(mockUser);
  
    when(sampleUserQuery.list()).thenReturn(mockUsers);
    when(sampleUserQuery.memberOfGroup(anyString())).thenReturn(sampleUserQuery);
    when(sampleUserQuery.count()).thenReturn((long) mockUsers.size());
  
    GroupQuery sampleGroupQuery = mock(GroupQuery.class);
    
    List<Group> mockGroups = MockProvider.createMockGroups();
    when(sampleGroupQuery.list()).thenReturn(mockGroups);
    when(sampleGroupQuery.groupMember(anyString())).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupName()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupId()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.orderByGroupType()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.asc()).thenReturn(sampleGroupQuery);
    when(sampleGroupQuery.desc()).thenReturn(sampleGroupQuery);
  
    when(processEngine.getIdentityService().createGroupQuery()).thenReturn(sampleGroupQuery);
    when(processEngine.getIdentityService().createUserQuery()).thenReturn(sampleUserQuery);
  }
  

  @Test
  public void testGroupInfoQuery() {
    given().queryParam("userId", "name")
        .then().expect().statusCode(Status.OK.getStatusCode())
        .body("groups.size()", is(1))
        .body("groups[0].id", equalTo(MockProvider.EXAMPLE_GROUP_ID))
        .body("groups[0].name", equalTo(MockProvider.EXAMPLE_GROUP_NAME))
        .body("groupUsers.size()", is(1))
        .body("groupUsers[0].id", equalTo(mockUser.getId()))
        .when().get(TASK_GROUPS_URL);
  }
  
  @Test
  public void testGroupInfoQueryWithMissingUserParameter() {
    expect().statusCode(Status.BAD_REQUEST.getStatusCode()).contentType(ContentType.JSON)
    .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
    .body("message", equalTo("No user id was supplied"))
    .when().get(TASK_GROUPS_URL);
  }
}
