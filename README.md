````
  ____  ____ _____   ____                 _                                  _   
 / ___||  _ \_   _| |  _ \  _____   _____| | ___  _ __  _ __ ___   ___ _ __ | |_ 
 \___ \| |_) || |   | | | |/ _ \ \ / / _ \ |/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
  ___) |  __/ | |   | |_| |  __/\ V /  __/ | (_) | |_) | | | | | |  __/ | | | |_ 
 |____/|_|    |_|   |____/ \___| \_/ \___|_|\___/| .__/|_| |_| |_|\___|_| |_|\__|
                                                 |_|                                           
 logging-spring -----------------------------------------------------------------
````

[![build_status](https://github.com/spt-development/spt-development-logging-spring/actions/workflows/build.yml/badge.svg)](https://github.com/spt-development/spt-development-logging-spring/actions)

A library for adding logging (at the start, end and on exception) to public methods of classes annotated with 
`@RestController`, `@Service` or `@Repository` or methods annotated with `@JmsListener` or methods of sub-classes
of `org.springframework.dao.support.DaoSupport`.

Usage
=====

Register the Aspects as Spring Beans manually or by adding the
[spt-development/spt-development-logging-spring-boot](https://github.com/spt-development/spt-development-logging-spring-boot)
starter to your project's pom.

```java
import com.spt.development.logging.spring.DaoSupportLogger;

import java.beans.BeanProperty;

@Bean
public RestControllerLogger restControllerLogger() {
    return new RestControllerLogger();
}

@Bean
public JmsListenerLogger jmsListenerLogger() {
    return new JmsListenerLogger();
}

@Bean
public ServiceLogger serviceLogger() {
    return new ServiceLogger();
}

@Bean
public RepositoryLogger repositoryLogger() {
    return new RepositoryLogger();
}

@Bean
public DaoSupportLogger daoSupportLogger() {
    return new DaoSupportLogger();
}
```

*NOTE* The `DaoSupportLogger` will result in warnings such as the following being logged by `CglibAopProxy`:

`Unable to proxy interface-implementing method [public final void org.springframework.dao.support.DaoSupport.afterPropertiesSet() throws java.lang.IllegalArgumentException,org.springframework.beans.factory.BeanInitializationException]
because it is marked as final, consider using interface-based JDK proxies instead.`

The aspects above rely on classes either extending specific base classes or being annotated with particular annotations. `BeanLogger` has
been introduced to add logging to arbitrary beans whether they are annotated with annotations such as `@Component` or whether they are
instantiated with a factory method. Logging for arbitrary beans is enabled with the `EnableBeanLogging` annotation:

```java
@Configuration
@EnableBeanLogging(
    excludedClasses = MyExcludedBean.class,
    includeBasePackageClasses = {
        MyLoggedBean.class
    }
)
static class MyConfiguration {
    @Bean
    MyLoggedClass myLoggedBean() {
        return new MyLoggedClass();
    }

    @Bean
    MyExcludedBean myExcludedBean() {
        return new MyExcludedBean();
    }
}
```

*NOTE* `excludedClasses` only excludes the class from logging being added with `BeanLogger` and as such, logging could still be added 
with one of the other aspects from this librar, such as `DaoSupportLogger` if it extends the right base class or is annotated with the
right annotation.

Building locally
================

To build the library, run the following maven command:

```shell
$ ./mvnw clean install
```

Release
=======

To build a release and upload to Maven Central push to `main`.
