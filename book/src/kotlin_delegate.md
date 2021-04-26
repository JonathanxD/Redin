# Kotlin Delegate

Redin supports dependency resolution using kotlin delegate (`by`) through `Provide` and `Get` classes:

```kotlin
class MyService @Inject constructor(val injector: Injector) {
    val logger: Logger by Provide(this.injector, scope = SINGLETON)
}

fun kotlinDelegateInject() {
    val injector = Redin {
        bind<Logger>().inSingletonScope().toValue(Logger.getGlobal())
        bindToImplementation<MyService>(scope = SINGLETON)
    }

    val myService = injector.provide<MyService>(scope = SINGLETON)()
}
```
