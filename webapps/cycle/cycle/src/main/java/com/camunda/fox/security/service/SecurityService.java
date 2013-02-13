package com.camunda.fox.security.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.springframework.stereotype.Component;

import com.camunda.fox.cycle.connector.crypt.EncryptionService;
import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.license.FoxLicenseService;
import com.camunda.fox.license.entity.FoxComponent;
import com.camunda.fox.license.impl.FoxLicenseException;
import com.camunda.fox.license.impl.FoxLicenseNotFoundException;
import com.camunda.fox.security.SecurityConfiguration;
import com.camunda.fox.security.UserIdentity;
import com.camunda.fox.security.UserLookup;
import com.camunda.fox.security.jaas.PassiveCallbackHandler;

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
  
    
  public UserIdentity login(String userName, String password) throws FoxLicenseException, FoxLicenseNotFoundException {
    if (userName == null || password == null) {
      return null;
    }
    
    checkLicense();

    if (config.isUseJaas()) {
      return loginViaJaas(userName, password);
    } else {
      return loginViaUserManagement(userName, password);
    }
  }

  protected void checkLicense() throws FoxLicenseException, FoxLicenseNotFoundException {
    FoxLicenseService foxLicenseService = config.getFoxLicenseService();
    foxLicenseService.checkLicenseFor(FoxComponent.FOX_CYCLE);      
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
