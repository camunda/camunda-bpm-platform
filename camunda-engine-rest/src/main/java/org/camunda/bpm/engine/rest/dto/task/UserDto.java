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
}
