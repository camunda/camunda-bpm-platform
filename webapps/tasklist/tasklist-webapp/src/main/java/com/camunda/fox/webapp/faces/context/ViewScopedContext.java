package com.camunda.fox.webapp.faces.context;
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

/**
 * This class provides the contexts lifecycle for the new JSF-2 &#064;ViewScoped Context
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Copied from seam-faces.
 */
public class ViewScopedContext implements Context, SystemEventListener {

    private final static String COMPONENT_MAP_NAME = "org.jboss.seam.faces.viewscope.componentInstanceMap";
    private final static String CREATIONAL_MAP_NAME = "org.jboss.seam.faces.viewscope.creationalInstanceMap";

    private boolean isJsfSubscribed = false;

    @SuppressWarnings("unchecked")
    public <T> T get(final Contextual<T> component) {
        assertActive();

        if (!isJsfSubscribed) {
            FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PreDestroyViewMapEvent.class, this);
            isJsfSubscribed = true;
        }

        T instance = (T) getComponentInstanceMap().get(component);

        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        assertActive();

        T instance = get(component);
        if (instance == null) {
            if (creationalContext != null) {
                Map<Contextual<?>, Object> componentInstanceMap = getComponentInstanceMap();
                Map<Contextual<?>, CreationalContext<?>> creationalContextMap = getCreationalInstanceMap();

                synchronized (componentInstanceMap) {
                    instance = (T) componentInstanceMap.get(component);
                    if (instance == null) {
                        instance = component.create(creationalContext);
                        if (instance != null) {
                            componentInstanceMap.put(component, instance);
                            creationalContextMap.put(component, creationalContext);
                        }
                    }
                }
            }
        }

        return instance;
    }

    public Class<? extends Annotation> getScope() {
        return ViewScoped.class;
    }

    public boolean isActive() {
        return getViewRoot() != null;
    }

    private void assertActive() {
        if (!isActive()) {
            throw new ContextNotActiveException(
                    "Seam context with scope annotation @ViewScoped is not active with respect to the current thread");
        }
    }

    public boolean isListenerForSource(final Object source) {
        if (source instanceof UIViewRoot) {
            return true;
        }

        return false;
    }

    /**
     * We get PreDestroyViewMapEvent events from the JSF servlet and destroy our contextual instances. This should
     * (theoretically!) also get fired if the webapp closes, so there should be no need to manually track all view scopes and
     * destroy them at a shutdown.
     *
     * @see javax.faces.event.SystemEventListener#processEvent(javax.faces.event.SystemEvent)
     */
    @SuppressWarnings("unchecked")
    public void processEvent(final SystemEvent event) {
        if (event instanceof PreDestroyViewMapEvent) {
            Map<Contextual<?>, Object> componentInstanceMap = getComponentInstanceMap();
            Map<Contextual<?>, CreationalContext<?>> creationalContextMap = getCreationalInstanceMap();

            if (componentInstanceMap != null) {
                for (Entry<Contextual<?>, Object> componentEntry : componentInstanceMap.entrySet()) {
                    /*
                     * No way to inform the compiler of type <T> information, so it has to be abandoned here :(
                     */
                    Contextual contextual = componentEntry.getKey();
                    Object instance = componentEntry.getValue();
                    CreationalContext creational = creationalContextMap.get(contextual);

                    contextual.destroy(instance, creational);
                }
            }
        }
    }

    protected UIViewRoot getViewRoot() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context != null) {
            return context.getViewRoot();
        }

        return null;
    }

    protected Map<String, Object> getViewMap() {
        UIViewRoot viewRoot = getViewRoot();

        if (viewRoot != null) {
            return viewRoot.getViewMap(true);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<Contextual<?>, Object> getComponentInstanceMap() {
        Map<String, Object> viewMap = getViewMap();
        Map<Contextual<?>, Object> map = (ConcurrentHashMap<Contextual<?>, Object>) viewMap.get(COMPONENT_MAP_NAME);

        if (map == null) {
            map = new ConcurrentHashMap<Contextual<?>, Object>();
            viewMap.put(COMPONENT_MAP_NAME, map);
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<Contextual<?>, CreationalContext<?>> getCreationalInstanceMap() {
        Map<String, Object> viewMap = getViewMap();
        Map<Contextual<?>, CreationalContext<?>> map = (Map<Contextual<?>, CreationalContext<?>>) viewMap
                .get(CREATIONAL_MAP_NAME);

        if (map == null) {
            map = new ConcurrentHashMap<Contextual<?>, CreationalContext<?>>();
            viewMap.put(CREATIONAL_MAP_NAME, map);
        }

        return map;
    }
}
