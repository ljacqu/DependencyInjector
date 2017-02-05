
#### 0.4
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

#### 0.3
- Allow to supply `Provider` types for custom instantiation
- Implement injection of `Provider` into other classes
- Fix test runner to allow multiple `@InjectMocks` fields in one test class
- Add new injector method: `createIfHasDependencies`
- Calling `Injector#newInstance` also goes through all PreConstructHandlers first

[:green_book: All issues in 0.3](https://github.com/ljacqu/DependencyInjector/milestone/3?closed=1)

#### 0.2
- Add `@NoMethodScan` and `@NoFieldScan` to prevent `NoClassDefFoundError` in classes using an optional dependency
- Throw exception in default injection providers if `@Inject` is present on multiple members of different type

[:green_book: All issues in 0.2](https://github.com/ljacqu/DependencyInjector/milestone/2?closed=1)


#### 0.1
- Initial commit — create injector logic
- Implement handler architecture to allow custom behavior

[:green_book: All issues in 0.1](https://github.com/ljacqu/DependencyInjector/milestone/1?closed=1)