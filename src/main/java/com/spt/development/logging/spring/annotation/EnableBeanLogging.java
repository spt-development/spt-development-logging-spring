package com.spt.development.logging.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables the logging of calls to all public methods of classes belong to the same package(s) of any classes
 * specified with includeBasePackageClasses and not specifically excluded with excludedClasses.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(BeanLoggerConfiguration.class)
public @interface EnableBeanLogging {

    /**
     * Enables logging of Spring beans from the same package as any of the classes specified.
     *
     * @return base packages to log.
     */
    Class<?>[] includeBasePackageClasses() default {};

    /**
     * Excludes specific classes from being logged. NOTE. Classes in this array, will still be logged
     * if logging is included with any of the other aspects defined in this library, such as
     * {@link com.spt.development.logging.spring.DaoSupportLogger}.
     *
     * @return classes to exclude from logging
     */
    Class<?>[] excludedClasses() default {};
}
