package org.camunda.bpm.security.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.camunda.bpm.cycle.connector.crypt.EncryptionService;
import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.security.SecurityConfiguration;
import org.camunda.bpm.security.UserIdentity;
import org.camunda.bpm.security.UserLookup;
import org.camunda.bpm.security.jaas.PassiveCallbackHandler;
import org.springframework.stereotype.Component;


/**
 *
 * @author nico.rehwaldt
 */
@Component
public class SecurityService {

  private static final Logger logger = Logger.getLogger(SecurityService.class.getSimpleName());
  
  @Inject
  private SecurityConfiguration config;
  @Inject
  private UserLookup userLookup;
  @Inject
  private EncryptionService encryptionService;
  
    
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
      logger.log(Level.WARNING, "Unable to login via JAAS.", e);
      return null;
    }
  }

  private UserIdentity loginViaUserManagement(String userName, String password) {
    User user = userLookup.findByName(userName);
    
    if (user == null) {
      return null;
    }
    
    if (encryptionService.checkUserPassword(password, user.getPassword())) {
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
