
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