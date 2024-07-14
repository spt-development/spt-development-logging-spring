package com.spt.development.logging.spring.annotation;

import com.spt.development.logging.spring.BeanLogger;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.spt.development.logging.spring.LoggerUtil.LOGGING_DISABLED_POINTCUT_EXPRESSION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link Configuration} for {@link BeanLogger}.
 */
@Role(
    ROLE_INFRASTRUCTURE
)
@EnableAspectJAutoProxy
@Configuration(proxyBeanMethods = false)
public class BeanLoggerConfiguration implements ImportAware {
    private Set<Class<?>> includeBasePackageClasses = Collections.emptySet();
    private Set<Class<?>> excludedClasses = Collections.emptySet();

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        final AnnotationAttributes attributes = AnnotationAttributes.fromMap(
            importMetadata.getAnnotationAttributes(
                EnableBeanLogging.class.getName(), false)
        );

        if (attributes != null) {
            includeBasePackageClasses = new HashSet<>(Arrays.asList(attributes.getClassArray("includeBasePackageClasses")));

            if (CollectionUtils.isEmpty(includeBasePackageClasses)) {
                throw new IllegalStateException(
                    "Property 'includeBasePackageClasses' must contain at least one class, when enabling bean logging with @EnableBeanLogger"
                        + " annotation"
                );
            }
            excludedClasses = new HashSet<>(Arrays.asList(attributes.getClassArray("excludedClasses")));
        }
    }

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    public AspectJExpressionPointcutAdvisor beanLogger(@Value("${spt.cid.mdc.disabled:false}") final boolean mdcDisabled) {
        final AspectJExpressionPointcutAdvisor pointcutAdvisor = new AspectJExpressionPointcutAdvisor();

        pointcutAdvisor.setExpression(buildPointcutAdvisorExpression());
        pointcutAdvisor.setAdvice(new BeanLogger(mdcDisabled));

        return pointcutAdvisor;
    }

    private String buildPointcutAdvisorExpression() {
        final StringBuilder sb = new StringBuilder("(");

        // Assumes creation of bean will have failed in setImportMetadata, if includeBasePackageClasses was empty
        for (Class<?> includeBasePackage : includeBasePackageClasses) {
            if (sb.length() > 1) {
                sb.append(" || ");
            }

            // Add logging to all public methods in the package
            sb.append("execution(* ")
                .append(includeBasePackage.getPackageName())
                .append("..*(..))");
        }
        sb.append(')');

        for (Class<?> excludedClass : excludedClasses) {
            // Exclude logging from all methods in the class
            sb.append(" && !execution(* ")
                .append(excludedClass.getName())
                .append("..*(..))");
        }

        sb.append(" && !(")
            .append(LOGGING_DISABLED_POINTCUT_EXPRESSION)
            .append(')');

        return sb.toString();
    }
}
