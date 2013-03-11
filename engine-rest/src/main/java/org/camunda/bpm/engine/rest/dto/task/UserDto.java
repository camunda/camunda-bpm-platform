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
package org.camunda.bpm.engine.rest.dto.task;

/**
 * @author: drobisch
 */
public class UserDto {
  private String firstName;
  private String lastName;
  private String displayName;

  private String id;

  public UserDto(String id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;

    if (firstName == null && lastName == null) {
      this.displayName = id;
    }else {
      this.displayName = (lastName != null) ? firstName + " " + lastName : firstName;
    }
  }

  public String getFirstName() {
    return firstName;
  }

  public String getId() {
    return id;
  }

  public String getLastName() {
    return lastName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserDto userDto = (UserDto) o;

    if (firstName != null ? !firstName.equals(userDto.firstName) : userDto.firstName != null) return false;
    if (id != null ? !id.equals(userDto.id) : userDto.id != null) return false;
    if (lastName != null ? !lastName.equals(userDto.lastName) : userDto.lastName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = firstName != null ? firstName.hashCode() : 0;
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }
}
