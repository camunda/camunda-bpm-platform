package org.camunda.bpm.tasklist.dto;

/**
 * @author : drobisch
 */
public class AuthenticationResponse {
  boolean success;
  String user;

  public AuthenticationResponse(boolean success, String user) {
    this.success = success;
    this.user = user;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
