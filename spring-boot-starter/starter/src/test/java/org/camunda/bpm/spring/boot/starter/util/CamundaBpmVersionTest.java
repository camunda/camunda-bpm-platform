package org.camunda.bpm.spring.boot.starter.util;

import org.junit.Test;
import org.springframework.core.env.PropertiesPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion.key;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaBpmVersionTest {

  private static final String CURRENT_VERSION = "7.9.0";

  public static CamundaBpmVersion camundaBpmVersion(final String version) {
    final Package pkg = mock(Package.class);
    when(pkg.getImplementationVersion()).thenReturn(version);
    return new CamundaBpmVersion(pkg);
  }

  @Test
  public void current_version() {
    final CamundaBpmVersion version =  new CamundaBpmVersion();
    assertThat(version.isEnterprise()).isFalse();
    assertThat(version.get()).startsWith(CURRENT_VERSION);

    final PropertiesPropertySource source = version.getPropertiesPropertySource();
    assertThat(source.getName()).isEqualTo("CamundaBpmVersion");
    final String versionFromPropertiesSource = (String) source.getProperty(key(CamundaBpmVersion.VERSION));
    assertThat(versionFromPropertiesSource).startsWith(CURRENT_VERSION);
    assertThat(source.getProperty(key(CamundaBpmVersion.FORMATTED_VERSION))).isEqualTo("(v" + versionFromPropertiesSource + ")");
    assertThat(source.getProperty(key(CamundaBpmVersion.IS_ENTERPRISE))).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void isEnterprise_true() throws Exception {
    assertThat(camundaBpmVersion("7.6.0-alpha3-ee").isEnterprise()).isTrue();
  }

  @Test
  public void isEnterprise_false() throws Exception {
    assertThat(camundaBpmVersion("7.6.0-alpha3").isEnterprise()).isFalse();
  }
}
