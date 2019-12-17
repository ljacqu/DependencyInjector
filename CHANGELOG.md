#### 1.0 (2017-11-25)
- Major changes to the internal model the injector uses
  - Merge all subtypes of Handler into one class (`Handler`) so that it can be easily understood and extended
  - Rename `Instantiation` to `Resolution`
  - Dependencies are now defined with an `ObjectIdentifier` instance, which in the future should allow for more
    features and customizations (especially in regards of supporting more annotations and generics)
- Create injectable types `Factory<T>` and `SingletonStore<T>` to easily handle instances of a given type
- Separate injector project into two modules: injector (core), and injector-extras for additional handlers

[:green_book: All issues in 1.0](https://github.com/ljacqu/DependencyInjector/milestone/4?closed=1)

#### 0.4.1 (2017-10-05)
- Fix Java 9 compatibility by adding dependency to `javax.annotation-api` (`@PostConstruct` annotation
  not shipped with the JDK 9)

[:green_book: All issues in 0.4.1](https://github.com/ljacqu/DependencyInjector/milestone/7?closed=1)

#### 0.4 (2017-02-05)
- Default behavior conforms more to `@Inject` Javadoc
  - Inheritance support: `@Inject` fields on super classes are now injected
  - By default, `DefaultInjectionProvider` is used, which does not allow static fields to be injected,
    nor may field injection be used in a class that has an `@Inject` constructor. Use `StandardInjectionProvider`
    if you want to make use of these features (closer to the `@Inject` Javadoc specification).
- Improve handler interfaces
  - Revise signatures
  - Add instantiation context (holds contextual information about the class requested to be instantiated)
- Annotation support for "Create if has dependencies" feature
- Possibility to cache instantiation methods: see `InstantiationCache` (not included by default, see Javadoc)

[:green_book: All issues in 0.4](https://github.com/ljacqu/DependencyInjector/milestone/6?closed=1)

#### 0.3 (2016-08-20)
- Allow to supply `Provider` types for custom instantiation
- Implement injection of `Provider` into other classes
- Fix test runner to allow multiple `@InjectMocks` fields in one test class
- Add new injector method: `createIfHasDependencies`
- Calling `Injector#newInstance` also goes through all PreConstructHandlers first

[:green_book: All issues in 0.3](https://github.com/ljacqu/DependencyInjector/milestone/3?closed=1)

#### 0.2 (2016-07-16)
- Add `@NoMethodScan` and `@NoFieldScan` to prevent `NoClassDefFoundError` in classes using an optional dependency
- Throw exception in default injection providers if `@Inject` is present on multiple members of different type

[:green_book: All issues in 0.2](https://github.com/ljacqu/DependencyInjector/milestone/2?closed=1)


#### 0.1 (2016-07-06)
- Initial commit — create injector logic
- Implement handler architecture to allow custom behavior

[:green_book: All issues in 0.1](https://github.com/ljacqu/DependencyInjector/milestone/1?closed=1)