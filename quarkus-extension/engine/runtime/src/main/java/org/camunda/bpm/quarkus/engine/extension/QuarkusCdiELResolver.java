package org.camunda.bpm.quarkus.engine.extension;

import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.impl.javax.el.ELException;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QuarkusCdiELResolver extends ELResolver
{
    private final BeanManager beanManager;
    private final Map<String, Optional<Object>> cachedProxies;

    public QuarkusCdiELResolver()
    {
        beanManager = CDI.current().getBeanManager();
        cachedProxies = new ConcurrentHashMap<>();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext arg0, Object arg1)
    {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1)
    {
        return null;
    }

    @Override
    public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws ELException
    {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException
    {
        //we only check root beans
        if (base != null)
        {
            return null;
        }

        String beanName = (String) property;

        Optional<Object> contextualInstance = cachedProxies.get(beanName);
        if (contextualInstance == null)
        {
            contextualInstance = resolveProxy(beanName);
            cachedProxies.put(beanName, contextualInstance);
        }

        if (contextualInstance.isPresent())
        {
            context.setPropertyResolved(true);
            return contextualInstance.get();
        }

        return null;
    }

    protected Optional<Object> resolveProxy(String beanName)
    {
        Object contextualInstance = null;

        Set<Bean<?>> beans = beanManager.getBeans(beanName);
        if (beans != null && !beans.isEmpty())
        {
            Bean<?> bean = beanManager.resolve(beans);

            if (bean.getScope().equals(Dependent.class))
            {
                throw new IllegalArgumentException("@Dependent on beans used in EL are currently not supported! "
                        + " Class: " + bean.getBeanClass().toString());
            }

            CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
            contextualInstance = beanManager.getReference(bean, Object.class, creationalContext);
        }

        return contextualInstance == null ? Optional.empty() : Optional.of(contextualInstance);
    }

    @Override
    public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws ELException
    {
        return false;
    }

    @Override
    public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws ELException
    {

    }
}
