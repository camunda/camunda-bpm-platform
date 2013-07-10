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
package org.camunda.bpm.engine.rest.dto.identity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.AtomLink;
import org.camunda.bpm.engine.rest.dto.LinkableDto;

/**
 * @author Daniel Meyer
 *
 */
public class UserDto extends LinkableDto {
  
  protected String id;
  protected String firstName;
  protected String lastName;
  protected String email;
  protected String password;
  
  // links ////////////////////////////////////////////////

  public AtomLink generateLink(UriBuilder rootUriBuilder, String action, String relation) {
    
    UriBuilder builder = rootUriBuilder.path(ProcessInstanceRestService.class).path("{id}");
    if (action != null) {
      builder.path(action);
    }
    
    URI linkUri = builder.build(id);
    AtomLink link = new AtomLink(relation, linkUri.toString());
    return link;
    
  }
  
  // transformers ////////////////////////////////////////
  
  public static UserDto fromUser(User user) {
    UserDto result = new UserDto();
    result.id = user.getId();
    result.firstName = user.getFirstName();
    result.lastName = user.getLastName();
    result.email = user.getEmail();
    return result;
  }
  
  public static List<UserDto> fromUserList(List<User> sourceList) {
    List<UserDto> resultList = new ArrayList<UserDto>(); 
    for (User user : sourceList) {
      resultList.add(fromUser(user));
    }
    return resultList;
  }
  
  public void update(User dbUser) {  
    dbUser.setId(getId());
    dbUser.setFirstName(getFirstName());
    dbUser.setLastName(getLastName());
    dbUser.setEmail(getEmail());
    dbUser.setPassword(getPassword());  
  }
  

  // getter / setters ////////////////////////////////////
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
