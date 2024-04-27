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

Building locally
================

To build the library, run the following maven command:

```shell
$ ./mvnw clean install
```

Release
=======

To build a release and upload to Maven Central push to `main`.
