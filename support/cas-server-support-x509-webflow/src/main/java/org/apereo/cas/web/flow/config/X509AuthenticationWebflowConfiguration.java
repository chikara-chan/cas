package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.extractcert.X509CertificateExtractor;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.X509CertificateCredentialsRequestHeaderAction;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509AuthenticationWebflowConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("x509CertificateExtractor")
    private ObjectProvider<X509CertificateExtractor> x509CertificateExtractor;

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer x509WebflowConfigurer() {
        return new X509WebflowConfigurer(flowBuilderServices.getIfAvailable(),
            loginFlowDefinitionRegistry.getIfAvailable(),
            applicationContext, casProperties);
    }

    @Bean
    public Action x509Check() {
        val extractCertFromRequestHeader = casProperties.getAuthn().getX509().isExtractCert();
        if (extractCertFromRequestHeader) {
            return new X509CertificateCredentialsRequestHeaderAction(initialAuthenticationAttemptWebflowEventResolver.getIfAvailable(),
                serviceTicketRequestWebflowEventResolver.getIfAvailable(),
                adaptiveAuthenticationPolicy.getIfAvailable(),
                x509CertificateExtractor.getIfAvailable());
        }
        return new X509CertificateCredentialsNonInteractiveAction(initialAuthenticationAttemptWebflowEventResolver.getIfAvailable(),
            serviceTicketRequestWebflowEventResolver.getIfAvailable(),
            adaptiveAuthenticationPolicy.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "x509CasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer x509CasWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(x509WebflowConfigurer());
            }
        };
    }
}
