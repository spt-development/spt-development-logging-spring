````
  ____  ____ _____   ____                 _                                  _   
 / ___||  _ \_   _| |  _ \  _____   _____| | ___  _ __  _ __ ___   ___ _ __ | |_ 
 \___ \| |_) || |   | | | |/ _ \ \ / / _ \ |/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
  ___) |  __/ | |   | |_| |  __/\ V /  __/ | (_) | |_) | | | | | |  __/ | | | |_ 
 |____/|_|    |_|   |____/ \___| \_/ \___|_|\___/| .__/|_| |_| |_|\___|_| |_|\__|
                                                 |_|                                           
 logging-spring -----------------------------------------------------------------
````

[![build_status](https://travis-ci.com/spt-development/spt-development-logging-spring.svg?branch=main)](https://travis-ci.com/spt-development/spt-development-logging-spring)

A library for adding logging (at the start, end and on exception) to public methods of classes annotated with 
`@RestController`, `@Service` or `@Repository` or methods annotated with `@JmsListener`.

Usage
=====

Register the Aspects as Spring Beans manually or by adding the
[spt-development/spt-development-logging-spring-boot](https://github.com/spt-development/spt-development-logging-spring-boot)
starter to your project's pom.

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

Building locally
================

To build the library, run the following maven command:

    $ mvn clean install

Release
=======

To build a release and upload to Maven Central run the following maven command:

    $ export GPG_TTY=$(tty) # Required on Mac OS X
    $ mvn deploy -DskipTests -Prelease

NOTE. This is currently a manual step as not currently integrated into the build.
