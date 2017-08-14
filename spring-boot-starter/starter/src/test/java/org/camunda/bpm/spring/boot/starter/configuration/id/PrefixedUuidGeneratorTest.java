package org.camunda.bpm.spring.boot.starter.configuration.id;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixedUuidGeneratorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void prefixed_uuid() throws Exception {

    final String id = new PrefixedUuidGenerator("foo").getNextId();
    assertThat(id).startsWith("foo-");
    assertThat(id.split("-")).hasSize(6);
  }

  @Test
  public void fails_on_null() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("spring.application.name");

    new PrefixedUuidGenerator(null);
  }
}
