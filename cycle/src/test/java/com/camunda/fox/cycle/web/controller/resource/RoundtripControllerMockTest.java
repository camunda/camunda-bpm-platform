package com.camunda.fox.cycle.web.controller.resource;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  loader = SpringockitoContextLoader.class, 
  locations = {"classpath:/spring/context.xml", "classpath:/spring/test-*.xml"}
)
public class RoundtripControllerMockTest {
  
  @Inject
  @ReplaceWithMock
  private RoundtripRepository roundtripRepository;
  
  @Inject
  private RoundtripController roundtripController;
  
  @Test
  public void shouldAddRoundtrip() throws Exception {
    
    // given
    Roundtrip roundtrip = new Roundtrip(1l, "ASDF");
    RoundtripDTO dto = new RoundtripDTO(roundtrip);
    
    given(roundtripRepository.saveAndFlush(any(Roundtrip.class))).willReturn(roundtrip);
    
    // when
    RoundtripDTO response = roundtripController.create(dto);
    
    // then
    assertThat(response.getId(), is(roundtrip.getId()));
    assertThat(response.getName(), is(roundtrip.getName()));
    verify(roundtripRepository).saveAndFlush(any(Roundtrip.class));
  }
}
