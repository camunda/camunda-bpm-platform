package org.camunda.bpm.spring.boot.starter;

import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.test.pa.TestProcessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Svetlana Dorokhova.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = { TestProcessApplication.class, ProcessApplicationIT.DummyComponent.class },
  webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProcessApplicationIT {

  @Autowired
  private DummyComponent dummyComponent;

  @Test
  public void testPostDeployEvent() {
    assertThat(dummyComponent.isPostDeployEventOccurred()).isTrue();
  }

  @Component
  public static class DummyComponent {

    private boolean postDeployEventOccurred;

    @EventListener
    public void eventOccurred(PostDeployEvent event) {
      this.postDeployEventOccurred = true;
    }

    public boolean isPostDeployEventOccurred() {
      return postDeployEventOccurred;
    }
  }

}
