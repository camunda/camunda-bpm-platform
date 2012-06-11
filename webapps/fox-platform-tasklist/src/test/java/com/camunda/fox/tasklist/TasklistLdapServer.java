package com.camunda.fox.tasklist;

import java.util.HashSet;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.name.LdapDN;

/**
 * Main class to start a standalone LDAP Server based on
 * Apache DS
 * 
 */
public class TasklistLdapServer {

  private static final String BASE_DN = "o=camunda,c=com";

  private static final int PORT = 389;
  
  private DirectoryService service;
  private LdapServer ldapService;

  private Partition addPartition(String partitionId, String partitionDn) throws Exception {
    // Create a new partition named 'foo'.
    Partition partition = new JdbmPartition();
    partition.setId(partitionId);
    partition.setSuffix(partitionDn);
    service.addPartition(partition);

    return partition;
  }

  private void addIndex(Partition partition, String... attrs) {
    // Index some attributes on the apache partition
    HashSet<Index< ? , ServerEntry>> indexedAttributes = new HashSet<Index< ? , ServerEntry>>();

    for (String attribute : attrs) {
      indexedAttributes.add(new JdbmIndex<String, ServerEntry>(attribute));
    }

    ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
  }

  public void init() throws Exception {
    service = new DefaultDirectoryService();
    
    // Disable the ChangeLog system
    service.getChangeLog().setEnabled(false);
    service.setDenormalizeOpAttrsEnabled(true);    

    // Hint, OIDs can be checked online: http://www.oid-info.com/cgi-bin/display?oid=0.9.2342.19200300.100.1.1&action=display
    Partition camundaPartition = addPartition("camunda", BASE_DN);
    addIndex(camundaPartition, "objectClass", "ou", "uid");

    ldapService = new LdapServer();
    ldapService.setTransports(new TcpTransport(PORT));
    ldapService.setDirectoryService(service);

    service.startup();
    ldapService.start();


    // Create the root entry
    if (!service.getAdminSession().exists(camundaPartition.getSuffixDn())) {
      LdapDN dn = new LdapDN(BASE_DN);
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "domain"); 
      entry.add("dc", "camunda");
      service.getAdminSession().add(entry);
    }

    // create the users tree (see https://app.camunda.com/confluence/display/foxUserGuide/Using+LDAP+with+the+fox+tasklist)
    createGroup("office-berlin");
    String dnKermit = createUser("kermit", "office-berlin", "Kermit", "The Frog");
    String dnGonzo = createUser("gonzo", "office-berlin", "Gonzo", "The Great");
    String dnFozzie = createUser("fozzie", "office-berlin", "Fozzie", "Bear");

    createGroup("office-london");
    String dnOscar = createUser("oscar", "office-london", "Oscar", "The Crouch");
    String dnMonster = createUser("monster", "office-london", "Cookie", "Monster");

    createRole("management", dnKermit);
    createRole("clerk", dnKermit, dnGonzo, dnFozzie, dnOscar, dnMonster);
  }

  private String createUser(String user, String group, String firstname, String lastname) throws Exception {
    LdapDN dn = new LdapDN("uid="+user+",ou="+group+",o=camunda,c=com");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "person", "inetOrgPerson"); //, "extensibleObject"); //make extensible to allow for the "memberOf" field
      entry.add("uid", user);
      entry.add("cn", firstname);
      entry.add("sn", lastname);
      entry.add("userPassword", user.getBytes("UTF-8"));
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.toNormName());
    }
    return dn.toNormName();
  }

  private void createGroup(String name) throws InvalidNameException, Exception, NamingException {
    LdapDN dn = new LdapDN("ou=" + name + ",o=camunda,c=com");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "organizationalUnit");
      entry.add("ou", name);
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.toNormName());
    }
  }

  private void createRole(String roleName, String... users) throws Exception {
//    for (String userDn : users) {
//      LdapDN dn = new LdapDN(userDn);
//      service.getAdminSession().lookup(dn).add("memberOf", roleName);
//    }
    
    LdapDN dn = new LdapDN("ou=" + roleName + ",o=camunda,c=com");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "groupOfNames");
      entry.add("cn", roleName);
      for (String user : users) {        
        entry.add("member", user);
      }
      service.getAdminSession().add(entry);
    }
  }
  
  public static void main(String[] args) throws Exception {
      TasklistLdapServer server = new TasklistLdapServer();
      server.init();
      System.out.println("Started up LDAP Server on port " + PORT);
      // ads.service.shutdown();

      // test
//      LdapIdentityServiceImpl ldap = new LdapIdentityServiceImpl();
//      ldap.authenticateUser("kermit", "kermit");
//      System.out.println("Groups if kermit: " + ldap.getGroupsByUserId("kermit"));
//      System.out.println("Colleagues of gonzo:" + ldap.getColleaguesByUserId("gonzo"));
//      System.out.println("which seems to work fine :-)");
  }
}
