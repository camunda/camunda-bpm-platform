package com.camunda.fox.platform.tasklist.identity.ldap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.camunda.fox.platform.tasklist.identity.FoxIdentityService;
import com.camunda.fox.platform.tasklist.identity.User;

@Named
@ApplicationScoped
@Alternative
public class LdapIdentityServiceImpl implements FoxIdentityService, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public void authenticateUser(final String userId, String password) {
    String dn = executeLdapCommand(new GetDnForUserCommand(userId));

    // now try to log on with this user and the given password
    executeLdapCommand(new NoopCommand(), dn, password);
  }

  @Override
  public List<String> getGroupsByUserId(String userId) {
    return executeLdapCommand(new GetGroupsForUserCommand(userId));
  }

  @Override
  public List<User> getColleaguesByUserId(final String userId) {
    List<User> result = new ArrayList<User>();
    result.addAll(executeLdapCommand(new GetColleaguesForUserCommand(userId)));

    // Maybe add some hard coded colleagues for testing purposes
    // result.add(new User("kermit", "Kermit", "The Frog"));
    // result.add(new User("gonzo", "Gonzo", "The Great"));
    // result.add(new User("fozzie", "Fozzie", "Bear"));

    return result;
  }

  /**
   * execute an {@link LdapCommand} for the technical user (e.g. to query
   * colleagues or groups)
   */
  protected <T> T executeLdapCommand(LdapCommand<T> cmd) {
    return executeLdapCommand(cmd, LdapConstants.LDAP_USER, LdapConstants.LDAP_PASSWORD);
  }

  /**
   * execute an {@link LdapCommand} for the given user
   */
  protected <T> T executeLdapCommand(LdapCommand<T> cmd, String user, String password) {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.PROVIDER_URL, LdapConstants.LDAP_SERVER);
    if (LdapConstants.LDAP_USE_SSL) {
      // Remove if you have the SSL Key correctly in your keystore
      SslUnsecureTrustManagerHelper.accepptAllSSLCertificates();
      env.put(Context.SECURITY_PROTOCOL, "ssl");
    }
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, password);

    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(env);
      return cmd.execute(ctx);
    } catch (AuthenticationException ex) {
      throw new IllegalArgumentException("Invalid Credentials", ex);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Error while talking to LDAP server", ex);

    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (Exception e) {
          // Ignore
        }
      }
    }
  }
}
