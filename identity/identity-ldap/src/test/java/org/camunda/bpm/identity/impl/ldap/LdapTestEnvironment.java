/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.identity.impl.ldap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.camunda.bpm.engine.impl.util.IoUtil;

/**
 * <p>LDAP test setup using apache directory</p>
 * 
 * @author Bernd Ruecker
 * @author Daniel Meyer
 * 
 */
public class LdapTestEnvironment {
  
  private final static Logger LOG = Logger.getLogger(LdapTestEnvironment.class.getName());
  
  private static final String BASE_DN = "o=camunda,c=org";
  
  private DirectoryService service;
  private LdapServer ldapService;

  public void init() throws Exception {
    
    Properties properties = loadTestProperties();    
    String port = properties.getProperty("ldap.server.port");
    
    service = new DefaultDirectoryService();
    service.setWorkingDirectory(new File("target/ldap-work"));
    
    service.getChangeLog().setEnabled(false);
    service.setDenormalizeOpAttrsEnabled(true);    

    Partition camundaPartition = createPartition("camunda", BASE_DN);
    createIndex(camundaPartition, "objectClass", "ou", "uid");

    ldapService = new LdapServer();
    ldapService.setTransports(new TcpTransport(Integer.parseInt(port)));
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

    createGroup("office-berlin");
    String dnRoman = createUser("roman", "office-berlin", "Roman", "Smirnov", "roman@camunda.org");
    String dnRobert = createUser("robert", "office-berlin", "Robert", "Gimbel", "robert@camunda.org");
    String dnDaniel = createUser("daniel", "office-berlin", "Daniel", "Meyer", "daniel@camunda.org");

    createGroup("office-london");
    String dnOscar = createUser("oscar", "office-london", "Oscar", "The Crouch", "oscar@camunda.org");
    String dnMonster = createUser("monster", "office-london", "Cookie", "Monster", "monster@camunda.org");
    
    createGroup("office-home");
    String dnRuecker = createUser("ruecker", "office-home", "Bernd", "Ruecker", "ruecker@camunda.org");

    createRole("management", dnRuecker, dnRobert, dnDaniel);
    createRole("development", dnRoman, dnDaniel, dnOscar);
    createRole("consulting", dnRuecker);
    createRole("sales", dnRuecker, dnMonster);
  }

  protected String createUser(String user, String group, String firstname, String lastname, String email) throws Exception {
    LdapDN dn = new LdapDN("uid="+user+",ou="+group+",o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "person", "inetOrgPerson"); //, "extensibleObject"); //make extensible to allow for the "memberOf" field
      entry.add("uid", user);
      entry.add("cn", firstname);
      entry.add("sn", lastname);
      entry.add("mail", email);
      entry.add("userPassword", user.getBytes("UTF-8"));
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.toNormName());
    }
    return dn.toNormName();
  }

  protected void createGroup(String name) throws InvalidNameException, Exception, NamingException {
    LdapDN dn = new LdapDN("ou=" + name + ",o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "organizationalUnit");
      entry.add("ou", name);
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.toNormName());
    }
  }

  protected void createRole(String roleName, String... users) throws Exception {
    LdapDN dn = new LdapDN("ou=" + roleName + ",o=camunda,c=org");
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

  protected Partition createPartition(String partitionId, String partitionDn) throws Exception {
    Partition partition = new JdbmPartition();
    partition.setId(partitionId);
    partition.setSuffix(partitionDn);
    service.addPartition(partition);
    return partition;
  }

  protected void createIndex(Partition partition, String... attrs) {
    HashSet<Index< ? , ServerEntry>> indexedAttributes = new HashSet<Index< ? , ServerEntry>>();

    for (String attribute : attrs) {
      indexedAttributes.add(new JdbmIndex<String, ServerEntry>(attribute));
    }

    ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
  }

  public void shutdown() {
    try {
      service.shutdown();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "exception while shutting down ldap", e);
    }
  }
  
  protected Properties loadTestProperties() throws FileNotFoundException, IOException {
    Properties properties = new Properties();
    File file = IoUtil.getFile("ldap.properties");
    FileInputStream propertiesStream= null;
    try {
      propertiesStream = new FileInputStream(file);
      properties.load(propertiesStream);
    } finally {
      IoUtil.closeSilently(propertiesStream);
    }
    return properties;
  }

}
