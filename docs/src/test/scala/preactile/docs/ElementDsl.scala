package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object ElementDsl extends DocSpecSuite:
  def doc = page("Element DSL")(
    md"""
# Element DSL

Preactile provides a type-safe DSL for building HTML elements with props and events.
Elements live on `E`, attributes and events on `A`.

## Basic elements

All standard HTML elements are available as constructors:

```scala
import preactile.*

E.div(
  E.h1("Title"),
  E.p("A paragraph of text."),
  E.a(A.href("https://preactjs.com"), "Preact homepage"),
)
```

## Attributes and props

Use attribute constructors from `A` to set element attributes:

```scala
import preactile.*

E.input(
  A.`type`("text"),
  A.placeholder("Enter your name..."),
  A.value(""),
)
```

## Event handlers

Attach event listeners with typed callbacks:

```scala
import preactile.*

E.button(
  A.onClick(_ => println("Clicked!")),
  A.onMouseOver(_ => println("Hovered")),
  "Click me",
)
```

## Conditional rendering

Use `when` for conditional content:

```scala
import preactile.*

def statusBadge(isActive: Boolean) = E.div(
  when(isActive)(E.span(A.`class`("active"), "Online")),
  when(!isActive)(E.span(A.`class`("offline"), "Offline")),
)
```

## Live demo: elements, events, and conditionals

This component uses a checkbox input with an `onChange` handler to toggle state, then
conditionally renders content using `when`:
""",
    exampleDom("element-dsl-demo").fromSource(
      "docs/client/src/main/scala/preactile/docs/ElementDslDemo.scala",
      "demo",
    ),
    md"""
See the [Examples](/Examples) page for the other live demos.
""",
  )
end ElementDsl
