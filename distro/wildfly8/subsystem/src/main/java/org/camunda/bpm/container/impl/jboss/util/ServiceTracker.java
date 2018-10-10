package org.camunda.bpm.container.impl.jboss.util;

import java.util.Collection;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceName;

/**
 * <p>Service Listener that adds / removes services to / from a collection as they
 * are added / removed to the service controller.</p>
 * 
 * @author Daniel Meyer
 * 
 * @param <S>
 *          the type of the service to track
 */
public class ServiceTracker<S> extends AbstractServiceListener<Object> {
  
  protected Collection<S> serviceCollection;
  protected ServiceName typeToTrack;

  public ServiceTracker(ServiceName typeToTrack, Collection<S> serviceCollection) {
    this.serviceCollection = serviceCollection;
    this.typeToTrack = typeToTrack;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void transition(ServiceController controller, Transition transition) {
    
    if(!typeToTrack.isParentOf(controller.getName())) {
      return;
    }
    
    if(transition.getAfter().getState().equals(State.UP)) {
      serviceCollection.add((S) controller.getValue());
    } else  {
      serviceCollection.remove(controller.getValue());      
    }
    
  }

}

