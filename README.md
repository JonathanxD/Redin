![Redin Logo](logo.png)

**Injection made easy.**

Redin is a dependency injection framework which aims to be simple yet powerful.

Simple dependency injection framework. Redin was written on top of `KoresProxy` with some features like `Lazy` dependencies and `HotSwappable` implemented through the fast proxies of `KoresProxy`.

Redin is tiny, simple, and is not designed to inject complex dependencies.

## Examples

```kotlin
@RedinInject
class Example(val log: LoggingService)

fun example() {
    val injector = Redin {
        bind<LoggingService>() inScope SINGLETON toProvider { MyLoggingService() }
    }
    
    val example = injector.get<Example>()
}
```

### Module

```kotlin
@RedinInject
class Example(val log: LoggingService)
class MyModule {
    @Provides
    @Singleton
    fun provideLoggingService(): LoggingService = MyLoggingService()
}

fun example() {
    val injector = Redin {
        module(MyModule())
    }
    
    val example = injector.get<Example>()
}
```

## Qualifiers

```kotlin
@RedinInject
class Example(@Named("log") val log: LoggingService)
fun example() {
    val injector = Redin {
        bind<LoggingService>() qualifiedWith Name("log") toProvider { MyLoggingService() }
    }
    
    val example = injector.get<Example>()
}
```

## Lazy evaluated dependencies

```kotlin
@RedinInject
class Example(@LazyDep val log: LoggingService)
fun example() {
    val injector = Redin {
        bind<LoggingService>() toProvider { MyLoggingService() }
    }
    
    val example = injector.get<Example>() // Provider is not invoked here
    
    example.log.log("hey") // Provider is invoked here
}
```

#### For final types

```kotlin
@RedinInject
class Example(@LazyDep val log: Lazy<LoggingService>) // Kotlin Lazy
fun example() {
    val injector = Redin {
        bind<LoggingService>() toProvider { MyLoggingService() }
    }
    
    val example = injector.get<Example>() // Provider is not invoked here
    
    example.log.value.log("hey") // Provider is invoked here
}
```

## HotSwappable dependencies

```kotlin
@RedinInject
class Example(@HotSwappable val log: LoggingService)

fun example() {
    val injector = Redin {
        bind<LoggingService>() toProvider { MyLoggingService() }
    }
    
    val example = injector.get<Example>()
    
    example.log.log("hey")

    // This will hot-swap the [log] instance, but could be used to add more binds before calling `injector.get` again.
    injector.bind {
        bind<LoggingService>() toProvider { MyLoggingService() }
    }
    
    example.log.log("hey") // New logger instance invoked
}
```

#### For final types

Same as for `LazyDep`, but use `Hot` type instead of `Lazy`. Also there is some specialized `Hot` types like `HotInt`, `HotLong`, but you will not have any benefits using them (boxing and unboxing still needed).

## Late initialization dependencies (or optional dependencies).

```kotlin
@RedinInject
class Example(@Late val log: LoggingService) // You could also use LateInit type, like: @Late val log: LateInit.Ref<LoggingSerivce>

fun example() {
    val injector = Redin {}
    
    val example = injector.get<Example>()

    // This will initialize late bind (if applicable)
    injector.bind {
        bind<LoggingService>() toProvider { MyLoggingService() }
    }

    example.log.log("hey")
}
```

`@Late` used without `LateInit` will not be initialized (in other words, will be `null` or the default value), if used with `LateInit`, an uninitialized `LateInit` will be provided.

Note: If a dependency is available for a `@Late` at injection-time, this dependency will be used.

## Additional notes

`@HotSwappable` could be used with either `@Late` or `@Lazy`, and vice-versa (this does not applies if you are using `Hot`, `LateInit` or `Lazy` type). But you can't use `@Late` with `@Lazy` because `@Lazy` already implies late initialization.

## Child injectors

Child injectors are those that inherits bindings from a parent injector. In Redin, you could simply create a child injector by calling `child` function on an injector, for example:

```kotlin
fun example() {
    val injector = Redin {}
    val childInjector = injector.child {}
}
```

All expressions are the same as *Redin DSL*.

### Override bindings

In Redin, parent bindings could be overridden, just type the binding and you made it, example:

```kotlin
interface Logger {
    //...
}

class SysoutLogger : Logger
class FileLogger : Logger

@RedinInject
class MyService(val logger: Logger)

fun example() {
    val injector = Redin {
        bind<Logger>() toValue SysoutLogger()
    }

    val serviceLoggingToSysout = injector[MyService]

    val childInjector = injector.child {
        bind<Logger>() toValue FileLogger()
    }

    val serviceLoggingToFile = childInjector[MyService]
}
```

### Child injector and scopes

In Redin, child injectors does not automatically inherit scope injected instances, instead, new ones will be created when supplied, to change this behavior, you should call `Injector.inheritParentScopedBindingsCache()`, this will make all parent cached scoped bindings instance to be inherited and instances will be reused, if this is the desired behavior.

## JSR-330

Redin fully supports JSR-330.
