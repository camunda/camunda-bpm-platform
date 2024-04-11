package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectChangeTracker<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectChangeTracker.class);

    private final T object;
    private final Map<String, Object> initialState = new HashMap<>();

    private ObjectChangeTracker(T object) {
        this.object = object;
        captureInitialState();
    }

    public static <T> ObjectChangeTracker<T> of(T object) {
        return new ObjectChangeTracker<>(object);
    }

    private void captureInitialState() {
        for (Field field : getDeclaredFields()) {
            field.setAccessible(true);
            try {
                captureInitialState(field);
            } catch (IllegalAccessException e) {
                LOG.error("Error accessing field {}", field.getName());
            }
        }
    }

    public void restoreFields() {
        for (Field field : getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String fieldName = field.getName();
                Object oldValue = initialState.get(fieldName);
                Object newValue = field.get(object);

                if (oldValue != null && !oldValue.equals(newValue)) {
                    LOG.info("Field '{}' has changed from {} to {}. Restoring...", field.getName(), oldValue, newValue);
                    field.set(object, oldValue);
                }
            } catch (IllegalAccessException e) {
                LOG.error("Error accessing field {} due to {}", field.getName(), e.getMessage());
            }
        }
    }

    protected List<Field> getDeclaredFields() {
        List<Field> results = new ArrayList<>();

        if (object instanceof ProcessEngineConfiguration) {
            Class<?> klass = object.getClass();

            while (ProcessEngineConfiguration.class.isAssignableFrom(klass)) {
                addFieldsOfClassToResults(klass, results);
                klass = klass.getSuperclass();
            }

            return results;
        }

        addFieldsOfClassToResults(object.getClass(), results);
        return results;
    }

    private void addFieldsOfClassToResults(Class<?> klass, List<Field> results) {
        Field[] classFields = klass.getDeclaredFields();
        if (classFields.length > 0) {
            Collections.addAll(results, classFields);
        }
    }

    private void captureInitialState(Field field) throws IllegalAccessException {
//        LOG.debug("Capturing field {}", field.getName());

        if (Collection.class.isAssignableFrom(field.getType())) {
            // For collections, clone the original collection to capture its initial state
            Collection<?> originalCollection = (Collection<?>) field.get(object);
            Collection<?> cloned = cloneCollection(originalCollection);

            initialState.put(field.getName(), cloned);
        }  else if (Map.class.isAssignableFrom(field.getType())) {
            // For maps, clone the original map to capture its initial state
            Map<?, ?> originalMap = (Map<?, ?>) field.get(object);
            initialState.put(field.getName(), originalMap != null ? new HashMap<>(originalMap) : null);
        } else {
            initialState.put(field.getName(), field.get(object));
        }
    }

    public void clear() {
        initialState.clear();
    }

    public static Collection<?> cloneCollection(Collection<?> original) {
        if (original == null) {
            return null;
        }

        if (original instanceof List) {
            ArrayList<?> list = new ArrayList<>(original);
            return list;
        }

        if (original instanceof Set) {
            HashSet<?> set = new HashSet<>(original);
            return set;
        }

        // TODO might need to add more collections to clone here

        return original;
    }
}
