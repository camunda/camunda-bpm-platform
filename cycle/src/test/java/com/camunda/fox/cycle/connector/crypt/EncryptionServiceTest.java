package com.camunda.fox.cycle.connector.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.AssertThrows;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.aspect.LoginAspect;
import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorLoginMode;
import com.camunda.fox.cycle.connector.test.util.RepositoryUtil;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.repository.ConnectorCredentialsRepository;
import com.camunda.fox.cycle.security.IdentityHolder;
import com.camunda.fox.security.UserIdentity;

/**
 *
 * @author drobisch
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/test-*.xml"}
)
public class EncryptionServiceTest {

  @Inject
  protected LoginAspect loginAspect;
  
  private static final File TEST_DIRECTORY = new File("target/crypt-test");
  private static final String TEST_TEXT = "test_text";
  private static final String TEST_PASSWORD = "test_password";
  private static final String TEST_PASSWORD_FILE = "test_password42";
  
  private File passwordFile;

  /**
   * test inversion of encrypt and decrypt method
   */
  @Test
  public void testEncryption() {
    String testText = new EncryptionServiceImpl(TEST_PASSWORD).encryptConnectorPassword(TEST_TEXT);
    assertEquals(TEST_TEXT, new EncryptionServiceImpl(TEST_PASSWORD).decryptConnectorPassword(testText));
  }

  @Test(expected = EncryptionException.class)
  public void testReadingFromFile() {
    String testText = new EncryptionServiceImpl(TEST_PASSWORD).encryptConnectorPassword(TEST_TEXT);

    EncryptionServiceImpl service = new EncryptionServiceImpl();
    service.setPasswordFilePath(passwordFile.getPath());

    assertEquals(TEST_PASSWORD_FILE, service.getEncryptionPassword());
    assertFalse(service.getEncryptionPassword().isEmpty());

    assertNotSame(TEST_TEXT, service.decryptConnectorPassword(testText));
  }

  @Test
  public void testGlobalLoginAspectUsesEncryption() throws NoSuchMethodException, SecurityException {
    ArgumentCaptor<String> passwordArgument = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> userArgument = ArgumentCaptor.forClass(String.class);

    ConnectorConfiguration configuration = new ConnectorConfiguration();
    configuration.setGlobalPassword("ddPPbfzgkl3XtVLRZo2hzA=="); // "test" with default password
    configuration.setGlobalUser("test");
    configuration.setLoginMode(ConnectorLoginMode.GLOBAL);

    Connector connectorMock = Mockito.mock(Connector.class);
    Mockito.when(connectorMock.needsLogin()).thenReturn(true);
    Mockito.when(connectorMock.getConfiguration()).thenReturn(configuration);

    loginAspect.doLogin(connectorMock);

    Mockito.verify(connectorMock).login(userArgument.capture(), passwordArgument.capture());
    assertEquals("test", passwordArgument.getValue());
  }

  @Test
  public void testUserLoginAspectUsesEncryption() throws NoSuchMethodException, SecurityException {
    ArgumentCaptor<String> passwordArgument = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> userArgument = ArgumentCaptor.forClass(String.class);

    final String testPassword = "ddPPbfzgkl3XtVLRZo2hzA==";

    ConnectorConfiguration configuration = new ConnectorConfiguration();
    configuration.setId(1L);
    configuration.setGlobalPassword(testPassword); // "test" with default password
    configuration.setGlobalUser("test");
    configuration.setLoginMode(ConnectorLoginMode.USER);

    Connector connectorMock = Mockito.mock(Connector.class);
    Mockito.when(connectorMock.needsLogin()).thenReturn(true);
    Mockito.when(connectorMock.getConfiguration()).thenReturn(configuration);

    UserIdentity identity = new UserIdentity("testUser");
    IdentityHolder.setIdentity(identity);

    ConnectorCredentialsRepository credentialsRepository = Mockito.mock(ConnectorCredentialsRepository.class);
    ConnectorCredentials credentials = new ConnectorCredentials();
    credentials.setUsername("testUser");
    credentials.setPassword(testPassword);

    Mockito.when(credentialsRepository.findFetchAllByUsernameAndConnectorId(identity.getName(), configuration.getId())).thenReturn(credentials);

    loginAspect.setConnectorCredentialsRepository(credentialsRepository);
    loginAspect.doLogin(connectorMock);

    Mockito.verify(connectorMock).login(userArgument.capture(), passwordArgument.capture());
    assertEquals("test", passwordArgument.getValue());
  }
  
  @Test
  public void testUserPasswordEncryption() {
    String plain = "mypassword";
    
    EncryptionService service = new EncryptionServiceImpl();
    String digest = service.encryptUserPassword(plain);
    assertTrue(service.checkUserPassword(plain, digest));
  }

  @Before
  public void before() throws Exception {
    RepositoryUtil.clean(TEST_DIRECTORY);
    String testPath = RepositoryUtil.createVFSRepository(TEST_DIRECTORY);
    this.passwordFile = new File(testPath + File.separatorChar + "cycle.password");
    FileOutputStream stream = new FileOutputStream(passwordFile);
    stream.write(TEST_PASSWORD_FILE.getBytes());
    stream.close();
  }
}
