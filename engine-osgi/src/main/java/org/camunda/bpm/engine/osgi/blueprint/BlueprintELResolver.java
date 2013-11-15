package org.camunda.bpm.engine.osgi.blueprint;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * @see org.camunda.bpm.engine.test.spring.ApplicationContextElResolver
 */
public class BlueprintELResolver extends ELResolver {
	
  private static final Logger LOGGER = Logger.getLogger(BlueprintELResolver.class.getName());
	private Map<String, JavaDelegate> delegateMap = new HashMap<String, JavaDelegate>();

	public Object getValue(ELContext context, Object base, Object property) {
		if (base == null) {
			// according to javadoc, can only be a String
			String key = (String) property;
			for (String name : delegateMap.keySet()) {
	      if(name.equalsIgnoreCase(key)) {
	      	context.setPropertyResolved(true);
	      	return delegateMap.get(name);
	      }
	    }
		}

		return null;
	}
	
	public void bindService(JavaDelegate delegate, Map props) {
    String name = (String) props.get("osgi.service.blueprint.compname");
    delegateMap.put(name, delegate);
    LOGGER.info("added service to delegate cache " + name);
	}

	public void unbindService(JavaDelegate delegate, Map props) {
		String name = (String) props.get("osgi.service.blueprint.compname");
    if(delegateMap.containsKey(name)) {
    	delegateMap.remove(name);
    }
    LOGGER.info("removed service from delegate cache " + name);
	}

	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	public void setValue(ELContext context, Object base, Object property,
	    Object value) {
	}

	public Class<?> getCommonPropertyType(ELContext context, Object arg) {
		return Object.class;
	}

	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
	    Object arg) {
		return null;
	}

	public Class<?> getType(ELContext context, Object arg1, Object arg2) {
		return Object.class;
	}
}
