package com.camunda.fox.tasklist.identity;

public class User {

  private String username;
  private String firstname;
  private String lastname;

  public User(String username, String firstname, String lastname) {
    super();
    this.username = username;
    this.firstname = firstname;
    this.lastname = lastname;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

}
