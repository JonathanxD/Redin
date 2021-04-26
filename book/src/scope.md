# Scope

Scopes defines the “scope” of the dependency, the most common scope is the `SINGLETON` scope and `NO_SCOPE`.

## `NO_SCOPE`

Means that no scope is used, in this case, every time a dependency is requested, a new instance is produced (unless the dependency was bind using `toValue`).

## `SINGLETON`

Reuses already created instances everytime the dependency is requested.

## User-defined scopes

Works the same way as `SINGLETON`, however instances are shared across the user defined scope instead of `SINGLETON`.

### Implementing a scope

**TODO**