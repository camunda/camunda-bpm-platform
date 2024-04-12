package org.camunda.bpm.engine.test.cache.listener;

public class ProcessEngineObserverFactory {

    public static ProcessEngineObserver getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final ProcessEngineObserver INSTANCE = new ProcessEngineObserver();
    }
}