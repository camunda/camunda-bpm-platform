package org.camunda.bpm.cockpit.plugin.failedjobs;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.cockpit.plugin.failedjobs.resources.FailedJobsPluginRootResource;
import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

public class FailedJobsPlugin extends AbstractCockpitPlugin {

	public static final String ID = "failed-jobs-plugin";

	public String getId() {
		return ID;
	}

	@Override
	public Set<Class<?>> getResourceClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();

		classes.add(FailedJobsPluginRootResource.class);

		return classes;
	}
}
