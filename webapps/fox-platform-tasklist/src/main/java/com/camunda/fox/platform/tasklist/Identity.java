package com.camunda.fox.platform.tasklist;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.camunda.fox.platform.tasklist.event.SignInEvent;
import com.camunda.fox.platform.tasklist.event.SignOutEvent;

@SessionScoped
@Named
public class Identity implements Serializable {

  private static final long serialVersionUID = 1L;
  private User currentUser = new User();

  @Inject
  Event<SignInEvent> signInEvent;
  
  @Inject
  Event<SignOutEvent> signOutEvent;
  
  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  public void signIn() {
    signInEvent.fire(new SignInEvent());
  }

  public String signOut() {
    signOutEvent.fire(new SignOutEvent());
    this.currentUser = new User();
    return "../signin.jsf";
  }

  public boolean isSignedIn() {
    return currentUser.getUsername() != null && currentUser.getUsername().length() > 0 && currentUser.getPassword() != null
            && currentUser.getPassword().length() > 0;
  }
}
