package com.camunda.fox.platform.tasklist.identity.ldap;

import javax.naming.directory.DirContext;

/**
 * Noop Command, handy to check if log in was successfull
 */
public class NoopCommand extends LdapCommand<Void> {

  @Override
  public Void execute(DirContext ctx) throws Exception {
    // do nothing, if we made it to here, the login was successful :-)
    return null;
  }
}
