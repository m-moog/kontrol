# kontrol

kontrol is a kotlin framework in early development

> ⚠\
> kontrol is still in pre-alpha and highly experimental.\
> Things may not work correctly or get broken by new updates.\
> Features and functionality may be removed without prior notice.

## Current Features

✔ automatic instantiation and injection of classes\
✔ finding and injecting classes that implement an interface\
✔ automatic execution of classes

## Installation

> ℹ\
> kontrol is not available on any public repositories,\
> so you will have to clone the project and compile it yourself

````xml
<dependency>
    <groupId>org.kontrol</groupId>
    <artifactId>kontrol</artifactId>
    <version>pre-alpha</version>
</dependency>
````

## Getting started

### Initialization
````kotlin
fun main() = kontrol {
    packages = arrayOf(/*package names*/)
}
````

### kontrolled classes
The ``org.kontrol.Kontrolled`` interface is used to mark classes available for dependency injection.
A class can only be instantiated by kontrol if it has either:
- a zero argument constructor
- a constructor where all arguments have defaults
- a constructor that supports constructor injection

````kotlin
//valid
class InjectableClass: Kontrolled

//valid
class InjectableClass(
    val s: String = "this is a string",
    val i: Int = 42
): Kontrolled

//invalid
class InjectableClass(
    val s: String,
    val i: Int
): Kontrolled
````

#### Constructor injection
A constructor is available for constructor injection if all of its
arguments implement ``org.kontrol.Kontrolled``.

````kotlin
//valid, since there is a zero argument constructor
class Foo: Kontrolled

//valid, since there is a constructor where all arguments have defaults
class Bar(
    val s: String = "this is a String"
): Kontrolled

//valid, since there is a constructor where all arguments implement Kontrolled
class Baz(
    val foo: Foo,
    val bar: Bar
): Kontrolled

//invalid, since the aforementioned variants can not (yet) be mixed
class Baz(
    val foo: Foo,
    val s: String = "String"
): Kontrolled
````

#### Property injection
The ``org.kontrol.InjectionDelegate`` can be used to facilitate property injection. Only Classes
that implement ``org.kontrol.Kontrolled`` can be injected. Injection is lazy and happens on
first access.
````kotlin
class Foo(val bar: Bar): Kontrolled {
    private val baz: Baz by injection()
}
class Bar(val s: String = "this is a String"): Kontrolled
class Baz(val i: Int = 42): Kontrolled
````

### Executable
Classes that implement ``org.kontrol.Executable`` will be automatically executed
once kontrol has finished initialization

````kotlin
class Foo(val bar: Bar) : Executable {
    private val baz: Baz by injection()

    override fun execute() {
        Kontrol.logger.info { "it works!" }
        Kontrol.logger.info { "here is baz: $baz" }
    }
}
class Bar(val s: String = "this is a String") : Kontrolled
class Baz(val i: Int = 42) : Kontrolled
````