package com.camunda.fox.platform.tasklist.identity.ldap;

/*
 * Pretty chaotic class which needs some severe clean up.
 * 
 * Contains all configuration parameters for LDAP
 */
public class LdapConstants {

//  public static final boolean LDAP_USE_SSL = true;
//  public static final String LDAP_SERVER = "ldap://ldap.camunda.com:636/";
//  //private static final boolean LDAP_USE_SSL = true;
//  //private static final String LDAP_SERVER = "ldap://ldap.camunda.com:389/";
//  public static final String LDAP_PASSWORD = "";
//  public static final String LDAP_USER = "";

  // Properties for local ApacheDS
  public static final boolean LDAP_USE_SSL = false;
  public static final String LDAP_SERVER = "ldap://localhost:389/";
  public static final String LDAP_PASSWORD = "secret";
  public static final String LDAP_USER = "uid=admin,ou=system";

  public static String getBaseDn() {
    return "o=camunda,c=com";
  }
  
  public static String getUserIdAttribute() {
    return "uid";
  }

  public static String getGroupNameAttribute() {
    return "cn";
  }

//  public static String getGroupDn(String group) {
//    // uid="+user+",ou="+group+",dc=camunda,dc=com
//    //"ou=employee,o=camunda,c=com"
//    return "ou="+group+","+getBaseDn();
//  }

//  public static String getUserDn(String group, String user) {
//    //"uid=" + userId + ",ou=employee,o=camunda,c=com"
//    return "uid="+user+",ou="+group+","+getBaseDn();
//    
//  }


}
