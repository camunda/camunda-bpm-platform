package com.camunda.fox.cycle.repository;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.entity.Roundtrip;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/context.xml", "classpath:/spring/test-*.xml"}
)
public class AbstractRepositoryTest {

  @Inject
  private RoundtripRepository roundtripRepository;

  @After
  public void after() {
    // Remove all entities
    roundtripRepository.deleteAll();
  }

  @Test
  public void shouldDeleteAll() throws Exception {
    // given
    Roundtrip r1 = new Roundtrip("TestRoundtrip");
    Roundtrip r2 = new Roundtrip("TestRoundtrip2");
    Roundtrip r3 = new Roundtrip("TestRoundtrip3");
    
    // when
    roundtripRepository.saveAndFlush(r1);
    roundtripRepository.saveAndFlush(r2);
    roundtripRepository.saveAndFlush(r3);
    
    long count = roundtripRepository.countAll();
    int deleted = roundtripRepository.deleteAll();
    
    // then
    assertThat(count).isEqualTo(3);
    assertThat(deleted).isEqualTo(3);
    assertThat(roundtripRepository.findAll()).isEmpty();
  }

  @Test
  public void shouldSaveAndFlush() throws Exception {
    // given
    Roundtrip newRoundtrip = new Roundtrip("TestRoundtrip");
    
    // when
    newRoundtrip = roundtripRepository.saveAndFlush(newRoundtrip);
    Roundtrip roundtripFromDB = roundtripRepository.findById(newRoundtrip.getId());
    
    // then
    assertThat(roundtripRepository.findAll()).hasSize(1);
    assertThat(roundtripFromDB.getName()).isEqualTo(newRoundtrip.getName());
  }

  @Test
  public void shouldDeleteById() throws Exception {
    // given
    Roundtrip newRoundtrip = new Roundtrip("TestRoundtrip");
    
    // when
    newRoundtrip = roundtripRepository.saveAndFlush(newRoundtrip);
    roundtripRepository.delete(newRoundtrip);
    
    // then
    assertThat(roundtripRepository.findAll()).isEmpty();
  }
}
