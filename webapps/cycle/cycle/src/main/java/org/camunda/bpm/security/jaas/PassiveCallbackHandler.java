package org.camunda.bpm.security.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Provides user name and password without user interaction.
 *
 * @see
 * http://roneiv.wordpress.com/2008/02/19/perform-a-jaas-programmatic-login-in-jboss-try-to-solve-the-empty-remote-user-problem/
 *
 * @author nico.rehwaldt
 */
public class PassiveCallbackHandler implements CallbackHandler {

  private String userName;
  char[] password;

  /**
   * Create the callback handler with user name and password
   *
   * @param userName
   * @param password
   */
  public PassiveCallbackHandler(String userName, String password) {
    if (userName == null) {
      throw new IllegalArgumentException("user name is null");
    }

    if (password == null) {
      throw new IllegalArgumentException("user name is null");
    }

    this.userName = userName;
    this.password = password.toCharArray();
  }

  /**
   * Handle callbacks
   * 
   * @param callbacks
   * @throws java.io.IOException
   * @throws UnsupportedCallbackException 
   */
  @Override
  public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException {
    
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        ((NameCallback) callback).setName(userName);
      } else
      if (callback instanceof NameCallback) {
        ((NameCallback) callback).setName(userName);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }
}
