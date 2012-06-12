package com.camunda.fox.tasklist.api;

import java.io.Serializable;

public class TasklistUser implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;
  private String firstname;
  private String lastname;
  private String password;

  public TasklistUser() {
    this.username = "";
    this.password = "";
  }

  public TasklistUser(String username, String firstname, String lastname) {
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
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
