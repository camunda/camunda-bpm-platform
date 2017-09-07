package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.spring.boot.starter.CamundaBpmNestedRuntimeException;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersionTest;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnterLicenseKeyConfigurationTest {

  private final DataSource dataSource = new EmbeddedDatabaseBuilder()
      .generateUniqueName(true)
      .setType(EmbeddedDatabaseType.H2)
      .addScript("/org/camunda/bpm/engine/db/create/activiti.h2.create.engine.sql")
      .build();

  private final DataSource dataSourceWithSchema = new EmbeddedDatabaseBuilder()
    .generateUniqueName(true)
    .setType(EmbeddedDatabaseType.H2)
    .addScript("/org/camunda/bpm/spring/boot/starter/configuration/impl/createSchema.sql")
    .addScript("/org/camunda/bpm/engine/db/create/activiti.h2.create.engine.sql")
    .build();


  private void readLicenseKeyFromDataSource(DataSource dataSource, EnterLicenseKeyConfiguration enterLicenseKeyConfiguration) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      Optional<String> keyFromDatasource = enterLicenseKeyConfiguration.readLicenseKeyFromDatasource(connection);

      assertThat(keyFromDatasource.get())
        .isNotNull()
        .startsWith("1234567890")
        .endsWith("Github;unlimited")
        .doesNotContain("\n");
    }
  }

  private EnterLicenseKeyConfiguration createEnterLicenseKeyConfiguration(CamundaBpmProperties properties) {
    CamundaBpmVersion version = CamundaBpmVersionTest.camundaBpmVersion("123-ee");
    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = new EnterLicenseKeyConfiguration();
    ReflectionTestUtils.setField(enterLicenseKeyConfiguration, "version", version);
    ReflectionTestUtils.setField(enterLicenseKeyConfiguration, "camundaBpmProperties", properties);
    return enterLicenseKeyConfiguration;
  }

  private CamundaBpmProperties prepareCamundaBpmProperties(String tablePrefix, URL licenseFileUrl, DataSource dataSource, ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl configuration = mock(ProcessEngineConfigurationImpl.class);
    when(processEngine.getProcessEngineConfiguration()).thenReturn(configuration);

    when(configuration.getDataSource()).thenReturn(dataSource);
    CamundaBpmProperties properties = new CamundaBpmProperties();
    properties.setLicenseFile(licenseFileUrl);
    properties.getDatabase().setTablePrefix(tablePrefix);
    return properties;
  }



  @Test
  public void load_url_string() throws Exception {
    final URL licenseFileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource("camunda-license-dummy.txt");
    EnterLicenseKeyConfiguration licenseKeyConfiguration = new EnterLicenseKeyConfiguration();
    final String licenseKey = licenseKeyConfiguration
      .readLicenseKeyFromUrl(licenseFileUrl)
      .orElse(null);

    assertThat(licenseKey)
      .isNotNull()
      .startsWith("1234567890")
      .endsWith("Github;unlimited")
      .doesNotContain("\n");
  }


  @Test
  public void save_licenseKey() throws Exception {
    final String tablePrefix = "";
    final URL licenseFileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource("camunda-license-dummy.txt");
    final DataSource dataSource = this.dataSource;

    ProcessEngine processEngine = mock(ProcessEngine.class);
    CamundaBpmProperties properties = prepareCamundaBpmProperties(tablePrefix, licenseFileUrl, dataSource, processEngine);


    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = createEnterLicenseKeyConfiguration(properties);

    enterLicenseKeyConfiguration.postProcessEngineBuild(processEngine);

    readLicenseKeyFromDataSource(dataSource, enterLicenseKeyConfiguration);

  }



  @Test
  public void save_licenseKeyWithSchema() throws Exception {
    final String tablePrefix = "CAMUNDA.";
    final DataSource dataSource = this.dataSourceWithSchema;

    final URL licenseFileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource("camunda-license-dummy.txt");

    ProcessEngine processEngine = mock(ProcessEngine.class);
    CamundaBpmProperties properties = prepareCamundaBpmProperties(tablePrefix, licenseFileUrl, dataSource, processEngine);


    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = createEnterLicenseKeyConfiguration(properties);

    enterLicenseKeyConfiguration.postProcessEngineBuild(processEngine);

    readLicenseKeyFromDataSource(dataSource, enterLicenseKeyConfiguration);

  }



  @Test(expected = CamundaBpmNestedRuntimeException.class)
  public void save_licenseKeyWithSchemaFails() throws Exception {
    final String tablePrefix = "";
    final DataSource dataSource = this.dataSourceWithSchema;
    final URL licenseFileUrl = EnterLicenseKeyConfiguration.class.getClassLoader().getResource("camunda-license-dummy.txt");

    ProcessEngine processEngine = mock(ProcessEngine.class);
    CamundaBpmProperties properties = prepareCamundaBpmProperties(tablePrefix, licenseFileUrl, dataSource, processEngine);

    EnterLicenseKeyConfiguration enterLicenseKeyConfiguration = createEnterLicenseKeyConfiguration(properties);

    enterLicenseKeyConfiguration.postProcessEngineBuild(processEngine);

    readLicenseKeyFromDataSource(dataSource, enterLicenseKeyConfiguration);

  }

}
