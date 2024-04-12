package org.camunda.bpm.engine.test.cache.listener;

import org.camunda.bpm.engine.test.cache.event.ProcessEngineEvent;

import java.util.ArrayList;
import java.util.List;

public class ProcessEngineObserver {

    private final List<ProcessEngineEventListener> listeners;

    public ProcessEngineObserver() {
        this.listeners = new ArrayList<>();

        this.listeners.add(new LogListener());
        this.listeners.add(new AssignConfigListener());
        this.listeners.add(new CustomConfigListener());
    }

    public void update(ProcessEngineEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }

}
