# Circular dependency

A circular dependency scenario occurs when, for example, a dependency X depends on a dependency Y that depends on the dependency X. 

This could occur directly or indirectly, for example, the code below shows an example of a direct circular dependency:

```kotlin
class DependencyA @Inject constructor(val dependencyB: DependencyB)
class DependencyB @Inject constructor(val dependencyA: DependencyA)
```

and the code below shows and example of indirect circular dependency:

```kotlin
class DependencyA @Inject constructor(val dependencyB: DependencyB)
class DependencyB @Inject constructor(val dependencyC: DependencyC)
class DependencyC @Inject constructor(val dependencyA: DependencyA)
```

Redin does not have a mechanism to detect circular dependencies, because even if it is possible to do, given that Redin allows `Dependency Providers` which resolves dependencies dynamically, it will not be possible to cover all scenarios without adding extra work to users of Redin. So circular dependencies ends up in a `StackOverflowError`.

If you really need to have a circular dependency, use *lazy dependency resolution*:

```kotlin
class DependencyA @Inject constructor(val dependencyB: DependencyB)
class DependencyB @Inject constructor(@LazyDep val dependencyA: Lazy<DependencyA>)

fun circularInject() {
    val injector = Redin {
        bind<DependencyA>().inSingletonScope().toImplementation<DependencyA>()
        bind<DependencyB>().inSingletonScope().toImplementation<DependencyB>()
    }

    val dependency = injector.provide<DependencyA>(scope = SINGLETON)()

}
```

However, we heavily discourage the use of circular dependencies, as it does not follow the *separation of concerns* concept.

If you have a common interface, it would be interesting to use the proxied version of lazy, as it introduces *lazy initialization* without `Lazy` type indirection:

```kotlin
interface DependencyA
interface DependencyB

class DependencyAImpl @Inject constructor(val dependencyB: DependencyB) : DependencyA
class DependencyBImpl @Inject constructor(@LazyDep val dependencyA: DependencyA) : DependencyB


fun circularInject() {
    val injector = Redin {
        bind<DependencyA>().inSingletonScope().toImplementation<DependencyAImpl>()
        bind<DependencyB>().inSingletonScope().toImplementation<DependencyBImpl>()
    }

    val dependency = injector.provide<DependencyA>(scope = SINGLETON)()

}
```