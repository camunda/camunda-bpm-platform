package org.camunda.bpm.spring.boot.starter.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutorStartingEventListenerTest {

  @Mock
  private JobExecutor jobExecutor;

  @InjectMocks
  private JobExecutorStartingEventListener jobExecutorStartingEventListener;

  @Test
  public void handleProcessApplicationStartedEventTest() {
    JobExecutorStartingEventListener spy = Mockito.spy(jobExecutorStartingEventListener);
    spy.handleProcessApplicationStartedEvent(null);
    verify(spy).activate();
  }

  @Test
  public void activateIfNotStartedTest() {
    when(jobExecutor.isActive()).thenReturn(false);
    jobExecutorStartingEventListener.activate();
    verify(jobExecutor).start();
  }

  @Test
  public void doNotActivateIfAlreadyStartedTest() {
    when(jobExecutor.isActive()).thenReturn(true);
    jobExecutorStartingEventListener.activate();
    verify(jobExecutor, times(0)).start();
  }
}
