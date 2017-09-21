package org.apereo.cas.monitor.config;

import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.memcached.MemcachedPooledConnectionFactory;
import org.apereo.cas.monitor.MemcachedMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link MemcachedMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedMonitorConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MemcachedMonitorConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("kryoSerializableClasses")
    private Collection<Class<?>> kryoSerializableClasses = new ArrayList<>();

    @Bean
    public Monitor memcachedMonitor() {
        final MemcachedPooledConnectionFactory factory = 
                new MemcachedPooledConnectionFactory(casProperties.getMonitor().getMemcached(), this.kryoSerializableClasses);
        final ObjectPool<MemcachedClientIF> pool = new GenericObjectPool<>(factory);
        return new MemcachedMonitor(pool);
    }
}
