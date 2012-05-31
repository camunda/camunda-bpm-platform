package com.camunda.fox.platform.tasklist;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SessionScoped
@Named
public class Identity implements Serializable {

  private static final long serialVersionUID = 1L;
  private User currentUser = new User();

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  public void signIn() {
  }

  public String signOut() {
    this.currentUser = new User();
    return "../signin.jsf";
  }

  public boolean isSignedIn() {
    return currentUser.getUsername() != null && currentUser.getUsername().length() > 0 && currentUser.getPassword() != null
            && currentUser.getPassword().length() > 0;
  }
}
