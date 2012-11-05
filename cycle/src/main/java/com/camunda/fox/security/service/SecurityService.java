package com.camunda.fox.security.service;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.configuration.CycleConfiguration;
import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.security.UserIdentity;
import com.camunda.fox.security.UserIdentity;
import com.camunda.fox.security.jaas.PassiveCallbackHandler;

/**
 *
 * @author nico.rehwaldt
 */
@Component
public class SecurityService {

  @Inject
  private CycleConfiguration config;
  
  @Inject
  private UserRepository userRepository;
  
  public UserIdentity login(String userName, String password) {
    if (userName == null || password == null) {
      return null;
    }

    if (config.isUseJaas()) {
      return loginViaJaas(userName, password);
    } else {
      return loginViaUserManagement(userName, password);
    }
  }

  private UserIdentity loginViaJaas(String userName, String password) {
    try {
      // login via jaas
      LoginContext lc = new LoginContext("cycleRealm", new PassiveCallbackHandler(userName, password));
      lc.login();

      // get logged in subject
      Subject subject = lc.getSubject();
      
      // return principal
      return getOrCreateCycleIdentity(subject);
    } catch (LoginException e) {
      e.printStackTrace();
      return null;
    }
  }

  private UserIdentity loginViaUserManagement(String userName, String password) {
    User user = userRepository.findByName(userName);
    
    if (user == null) {
      return null;
    }
    
    // todo: encrypt
    if (user.getPassword().equals(password)) {
      return new UserIdentity(user);
    }

    return null;
  }

  private UserIdentity getOrCreateCycleIdentity(Subject subject) {
    
    System.out.println(subject);
    System.out.println(subject.getPrincipals());
    System.out.println(subject.getPublicCredentials());
    
    return null;
  }
}
