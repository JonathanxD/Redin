# Scope

Scopes defines the “scope” of the dependency, the most common scope is the `SINGLETON` scope and `NO_SCOPE`.

## `NO_SCOPE`

Means that no scope is used, in this case, every time a dependency is requested, a new instance is produced (unless the dependency was bind using `toValue`).

## `SINGLETON`

Reuses already created instances everytime the dependency is requested.

## User-defined scopes

Works the same way as `SINGLETON`, however instances are shared across the user defined scope instead of `SINGLETON`.

### Implementing a scope

Every scope is linked to an annotation, so to implement your own scope, you first need to have an annotation:

```kotlin
@Retention(AnnotationRetention.RUNTIME)
annotation class MyScope
```

Then you create an `object` to implement `BindScope`:

```kotlin
val MY_SCOPE = object : BindScope {
    override fun match(scope: AnnotationContainer): Boolean =
        scope.type.`is`(MyScope::class.java)

    override fun toString(): String = "MY_SCOPE"
}
```

The usage is the same as for `SINGLETON`:

```kotlin
class ScopeTest

fun myScope() {
    val injector = Redin {
        bind<ScopeTest>() inScope MY_SCOPE toImplementation(ScopeTest::class.java)
    }

    val test = injector.provide<ScopeTest>(scope = MY_SCOPE)()
    val test2 = injector.provide<ScopeTest>(scope = MY_SCOPE)()

    println(test)
    println(test2)
}
```