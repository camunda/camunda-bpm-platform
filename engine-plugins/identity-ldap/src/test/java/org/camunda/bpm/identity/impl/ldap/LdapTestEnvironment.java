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

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.api.ldap.model.name.Dn;
import org.camunda.bpm.engine.impl.util.IoUtil;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;

import org.apache.commons.io.FileUtils;

/**
 * <p>
 * LDAP test setup using apache directory</p>
 *
 * @author Bernd Ruecker
 * @author Daniel Meyer
 *
 */
public class LdapTestEnvironment {

  private final static Logger LOG = Logger.getLogger(LdapTestEnvironment.class.getName());

  private static final String BASE_DN = "o=camunda,c=org";

  protected DirectoryService service;
  protected LdapServer ldapService;
  protected String configFilePath = "ldap.properties";
  protected File workingDirectory = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );

  public LdapTestEnvironment() {
  }

  /**
   * initialize the schema manager and add the schema partition to directory
   * service
   *
   * @throws Exception if the schema LDIF files are not found on the classpath
   */
  protected void initSchemaPartition() throws Exception {
    InstanceLayout instanceLayout = service.getInstanceLayout();

    File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

    // Extract the schema on disk (a brand new one) and load the registries
    if (schemaPartitionDirectory.exists()) {
      LOG.log(Level.INFO, "schema partition already exists, skipping schema extraction");
    } else {
      SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
      extractor.extractOrCopy();
    }

    SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
    SchemaManager schemaManager = new DefaultSchemaManager(loader);

    // We have to load the schema now, otherwise we won't be able
    // to initialize the Partitions, as we won't be able to parse
    // and normalize their suffix Dn
    schemaManager.loadAllEnabled();

    List<Throwable> errors = schemaManager.getErrors();

    if (!errors.isEmpty()) {
      throw new Exception(I18n.err(I18n.ERR_317, Exceptions.printErrors(errors)));
    }

    service.setSchemaManager(schemaManager);

    // Init the LdifPartition with schema
    LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, service.getDnFactory());
    schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

    // The schema partition
    SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
    schemaPartition.setWrappedPartition(schemaLdifPartition);
    service.setSchemaPartition(schemaPartition);
  }

  /**
   * Initialize the server. It creates the partition, adds the index, and
   * injects the context entries for the created partitions.
   *
   * @throws Exception if there were some problems while initializing the system
   */
  protected void initializeDirectory() throws Exception {

    workingDirectory.mkdirs();

    service = new DefaultDirectoryService();
    InstanceLayout il = new InstanceLayout(workingDirectory);
    service.setInstanceLayout(il);

    CacheService cacheService = new CacheService();
    cacheService.initialize(service.getInstanceLayout());
    service.setCacheService(cacheService);

    initSchemaPartition();

    // then the system partition
    // this is a MANDATORY partition
    // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
    // while initializing
    JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager(), service.getDnFactory());
    systemPartition.setId("system");
    systemPartition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId()).toURI());
    systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
    systemPartition.setSchemaManager(service.getSchemaManager());

    // mandatory to call this method to set the system partition
    // Note: this system partition might be removed from trunk
    service.setSystemPartition(systemPartition);

    // Disable the ChangeLog system
    service.getChangeLog().setEnabled(false);
    service.setDenormalizeOpAttrsEnabled(true);

    Partition camundaPartition = addPartition("camunda", BASE_DN, service.getDnFactory());
    addIndex(camundaPartition, "objectClass", "ou", "uid");

    service.startup();

    // Create the root entry
    if (!service.getAdminSession().exists(camundaPartition.getSuffixDn())) {
      Dn dn = new Dn(BASE_DN);
      Entry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "domain", "extensibleObject");
      entry.add("dc", "camunda");
      service.getAdminSession().add(entry);
    }
  }

  /**
   * starts the LdapServer
   *
   * @throws Exception
   */
  public void startServer() throws Exception {
    ldapService = new LdapServer();
    Properties properties = loadTestProperties();
    String port = properties.getProperty("ldap.server.port");
    ldapService.setTransports(new TcpTransport(Integer.parseInt(port)));
    ldapService.setDirectoryService(service);
    ldapService.start();
  }

  public void init() throws Exception {
    initializeDirectory();
    startServer();

    createGroup("office-berlin");
    String dnRoman = createUserUid("roman", "office-berlin", "Roman", "Smirnov", "roman@camunda.org");
    String dnRobert = createUserUid("robert", "office-berlin", "Robert", "Gimbel", "robert@camunda.org");
    String dnDaniel = createUserUid("daniel", "office-berlin", "Daniel", "Meyer", "daniel@camunda.org");
    String dnGonzo = createUserUid("gonzo", "office-berlin", "Gonzo", "The Great", "gonzo@camunda.org");
    String dnRowlf = createUserUid("rowlf", "office-berlin", "Rowlf", "The Dog", "rowlf@camunda.org");
    String dnPepe = createUserUid("pepe", "office-berlin", "Pepe", "The King Prawn", "pepe@camunda.org");
    String dnRizzo = createUserUid("rizzo", "office-berlin", "Rizzo", "The Rat", "rizzo@camunda.org");

    createGroup("office-london");
    String dnOscar = createUserUid("oscar", "office-london", "Oscar", "The Crouch", "oscar@camunda.org");
    String dnMonster = createUserUid("monster", "office-london", "Cookie", "Monster", "monster@camunda.org");

    createGroup("office-home");
    // Doesn't work using backslashes, end up with two uid attributes
    // See https://issues.apache.org/jira/browse/DIRSERVER-1442
    String dnDavid = createUserUid("david(IT)", "office-home", "David", "Howe\\IT\\", "david@camunda.org");

    String dnRuecker = createUserUid("ruecker", "office-home", "Bernd", "Ruecker", "ruecker@camunda.org");

    createGroup("office-external");
    String dnFozzie = createUserCN("fozzie", "office-external", "Bear", "Fozzie", "fozzie@camunda.org");

    createRole("management", dnRuecker, dnRobert, dnDaniel);
    createRole("development", dnRoman, dnDaniel, dnOscar);
    createRole("consulting", dnRuecker);
    createRole("sales", dnRuecker, dnMonster, dnDavid);
    createRole("external", dnFozzie);
    createRole("all", dnRuecker, dnRobert, dnDaniel, dnRoman, dnOscar, dnMonster, dnDavid, dnFozzie, dnGonzo, dnRowlf, dnPepe, dnRizzo);
  }

  protected String createUserUid(String user, String group, String firstname, String lastname, String email) throws Exception {
    Dn dn = new Dn("uid=" + user + ",ou=" + group + ",o=camunda,c=org");
    createUser(user, firstname, lastname, email, dn);
    return dn.getNormName();
  }

  protected String createUserCN(String user, String group, String firstname, String lastname, String email) throws Exception {
    Dn dn = new Dn("cn=" + lastname + "\\," + firstname + ",ou=" + group + ",o=camunda,c=org");
    createUser(user, firstname, lastname, email, dn);
    return dn.getNormName();
  }

  protected void createUser(String user, String firstname, String lastname,
          String email, Dn dn) throws Exception, NamingException,
          UnsupportedEncodingException {
    if (!service.getAdminSession().exists(dn)) {
      Entry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "person", "inetOrgPerson"); //, "extensibleObject"); //make extensible to allow for the "memberOf" field
      entry.add("uid", user);
      entry.add("cn", firstname);
      entry.add("sn", lastname);
      entry.add("mail", email);
      entry.add("userPassword", user.getBytes("UTF-8"));
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.getNormName());
    }
  }

  protected void createGroup(String name) throws InvalidNameException, Exception, NamingException {
    Dn dn = new Dn("ou=" + name + ",o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      Entry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "organizationalUnit");
      entry.add("ou", name);
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.getNormName());
    }
  }

  protected void createRole(String roleName, String... users) throws Exception {
    Dn dn = new Dn("ou=" + roleName + ",o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      Entry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "groupOfNames");
      entry.add("cn", roleName);
      for (String user : users) {
        entry.add("member", user);
      }
      service.getAdminSession().add(entry);
    }
  }

  /**
   * Add a new partition to the server
   *
   * @param partitionId The partition Id
   * @param partitionDn The partition DN
   * @param dnFactory the DN factory
   * @return The newly added partition
   * @throws Exception If the partition can't be added
   */
  protected Partition addPartition(String partitionId, String partitionDn, DnFactory dnFactory) throws Exception {
    // Create a new partition with the given partition id
    JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
    partition.setId(partitionId);
    partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
    partition.setSuffixDn(new Dn(partitionDn));
    service.addPartition(partition);

    return partition;
  }

  /**
   * Add a new set of index on the given attributes
   *
   * @param partition The partition on which we want to add index
   * @param attrs The list of attributes to index
   */
  protected void addIndex(Partition partition, String... attrs) {
    // Index some attributes on the apache partition
    Set<Index<?, String>> indexedAttributes = new HashSet<Index<?, String>>();

    for (String attribute : attrs) {
      indexedAttributes.add(new JdbmIndex<String>(attribute, false));
    }

    ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
  }

  public void shutdown() {
    try {
      ldapService.stop();
      service.shutdown();
      if (workingDirectory.exists()) {
        FileUtils.deleteDirectory(workingDirectory);
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "exception while shutting down ldap", e);
    }
  }

  protected Properties loadTestProperties() throws IOException {
    Properties properties = new Properties();
    File file = IoUtil.getFile(configFilePath);
    FileInputStream propertiesStream = null;
    try {
      propertiesStream = new FileInputStream(file);
      properties.load(propertiesStream);
    } finally {
      IoUtil.closeSilently(propertiesStream);
    }
    return properties;
  }

}
