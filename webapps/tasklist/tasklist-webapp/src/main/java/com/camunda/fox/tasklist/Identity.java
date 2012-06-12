package com.camunda.fox.tasklist;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.camunda.fox.tasklist.api.TasklistIdentityService;
import com.camunda.fox.tasklist.api.TasklistUser;
import com.camunda.fox.tasklist.event.SignInEvent;
import com.camunda.fox.tasklist.event.SignOutEvent;

@SessionScoped
@Named
public class Identity implements Serializable {

  private static final Logger log = Logger.getLogger(Identity.class.getCanonicalName());
  
  private static final long serialVersionUID = 1L;
  private TasklistUser currentUser = new TasklistUser();

  @Inject
  private Event<SignInEvent> signInEvent;
  
  @Inject
  private Event<SignOutEvent> signOutEvent;
  
  @Inject
  private TasklistIdentityService foxIdentityService;

  @PostConstruct
  protected void init() {
    log.finest("initializing " + this.getClass().getSimpleName() + " (" + this + ")");
  }
  
  public TasklistUser getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(TasklistUser currentUser) {
    this.currentUser = currentUser;
  }

  public void signIn() {
    foxIdentityService.authenticateUser(currentUser.getUsername(), currentUser.getPassword());

    signInEvent.fire(new SignInEvent());
  }

  public String signOut() {
    signOutEvent.fire(new SignOutEvent());
    this.currentUser = new TasklistUser();
    return "../signin.jsf";
  }

  public boolean isSignedIn() {
    return currentUser.getUsername() != null && currentUser.getUsername().length() > 0 && currentUser.getPassword() != null
            && currentUser.getPassword().length() > 0;
  }
}
