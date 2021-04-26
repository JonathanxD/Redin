# Qualifiers

Qualifiers are used to identify between different dependencies of the same type. The most common qualifier is the `Named` qualifier:

```kotlin
val injector = Redin {
    bind<String>() inScope SINGLETON qualifiedWith Name("databaseUri") toValue "localhost:8090"
}
```

To query dependencies by qualifier, we need to use `AnnotationContainer`, however we heavily recommend providing wrappers around `AnnotationContainer` for the ease of use, just like we do for `Named` annotations:

```kotlin
val injector = Redin {
    bind<String>() inScope SINGLETON qualifiedWith Name("databaseUri") toValue "localhost:8090"
}

val uri = injector.provide<String>(scope = SINGLETON, qualifiers = listOf(nameQualifier("databaseUri")))()
```

## Implementing your own qualifiers

Just like implementing a [BindScope](scope.md), you need an annotation class (it must be annotated with `@Qualifier` to be detected as a qualifier):

```kotlin
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MyQualifier(val name: String)
```

A wrapper around `AnnotationContainer` for the ease of use:

```kotlin
fun myQualifier(name: String) =
    AnnotationContainer<MyQualifier>(mapOf("name" to name))
```

Then the matcher:

```kotlin
data class MyBindQualifier(val name: String) : BindQualifier {
    override fun matches(annotationContainer: AnnotationContainer): Boolean =
        annotationContainer.type.`is`(MyQualifier::class.java)
                && annotationContainer["name"] == name

}
```

### Using the qualifier

Use it just like any other qualifier:

```kotlin
class ToInject
class QualifierTest @Inject constructor(@MyQualifier("test") val inject: ToInject)

fun myQualifier() {
    val injector = Redin {
        bind<ToInject>() inScope SINGLETON qualifiedWith MyBindQualifier("test") toValue ToInject()
        bindToImplementation<QualifierTest>(scope = SINGLETON)
    }

    val qualifierTest = injector.provide<QualifierTest>(scope = SINGLETON)()
}
```

For querying using `provide`:

```kotlin
val toInject = injector.provide<ToInject>(scope = SINGLETON, qualifiers = listOf(myQualifier("test")))()
```
