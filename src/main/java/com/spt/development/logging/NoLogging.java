package com.spt.development.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be added at the class, method or parameter level for preventing all methods in the class, the
 * method with the annotation or the parameter with the annotation from being looged, respectively.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE })
public @interface NoLogging {
}
