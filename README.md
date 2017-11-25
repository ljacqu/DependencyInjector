## Lightweight Dependency Injector
[![Build Status](https://travis-ci.org/ljacqu/DependencyInjector.svg?branch=master)](https://travis-ci.org/ljacqu/DependencyInjector)
[![Coverage Status](https://coveralls.io/repos/github/ljacqu/DependencyInjector/badge.svg?branch=master)](https://coveralls.io/github/ljacqu/DependencyInjector?branch=master)
[![Javadocs](http://www.javadoc.io/badge/ch.jalu/injector.svg)](http://www.javadoc.io/doc/ch.jalu/injector)
[![Dependency Status](https://www.versioneye.com/user/projects/5768397dfdabcd004d3fcbd1/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5768397dfdabcd004d3fcbd1)
[![Code Climate](https://codeclimate.com/github/ljacqu/DependencyInjector/badges/gpa.svg)](https://codeclimate.com/github/ljacqu/DependencyInjector)

Simple but customizable dependency injector for Java 1.8 and above.


### Why use it
- Very lightweight (only has `javax.inject` and `javax.annotation-api` as dependency)
- Allows gradual transition to injection for existing projects
- You can implement your own injection methods and behaviors
- Support for projects with optional dependencies

### Integrating it
Using Maven, you can get the injector by adding this to your pom.xml:

```xml
<dependency>
    <groupId>ch.jalu</groupId>
    <artifactId>injector</artifactId>
    <version>1.0</version>
</dependency>
```

### Simple example
By default, the injector supports **constructor injection** and **field injection**.
Consider the following class skeletons:

```java
public class Settings {
  // regular class
}

public class Messages {
  private File messagesFile;

  @Inject
  Messages(Settings settings) {
    messagesFile = new File(settings.getLanguage() + ".txt");
  }
}

public class CalculationService {
  @Inject
  private Messages messages;
  
  @Inject
  private RoundingService roundingService;
}

public class RoundingService {
  private int precision;

  @Inject
  RoundingService(Settings settings) {
    precision = settings.getPrecision();
  }
}
```

At the startup of the application, we might only care about getting an instance of `CalculationService`. All other
classes are required by it to run, but we don't immediately care about them. With the injector, we don't have to
deal with those classes and can just retrieve what we actually want:

```java
public class MyApp {

  public static void main(String... args) {
    Injector injector = new InjectorBuilder().addDefaultHandlers("com.example.my.project").create();
    CalculationService calcService = injector.getSingleton(CalculationService.class);
    calcService.performCalculation();
  }
}
```

... That's all! No need to deal with creating any other classes, but you still have a setup that allows you to easily
unit test or switch a component.

--> Full, runnable example can be found [here](https://github.com/ljacqu/DependencyInjector/tree/master/injector/src/test/java/ch/jalu/injector/demo).

### Handlers
You may implement your own logic to instantiate classes and resolve dependencies. This allows you, for example, to
implement specific behavior for custom annotations.
Read more on the Wiki: [Handlers explained](https://github.com/ljacqu/DependencyInjector/wiki/Handlers)
