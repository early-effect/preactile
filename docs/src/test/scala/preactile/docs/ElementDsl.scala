package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object ElementDsl extends DocSpecSuite:
  def doc = page("Element DSL")(
    md"""
       # Element DSL

       Preactile provides a type-safe DSL for building HTML elements with props and events.

       ## Basic elements

       All standard HTML elements are available as functions:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       div(
         h1("Title"),
         p("A paragraph of text."),
         a(A.href("https://preactjs.com"), "Preact homepage"),
       )
       ```

       ## Attributes and props

       Use attribute constructors from `A` to set element attributes:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       input(
         A.`type`("text"),
         A.placeholder("Enter your name..."),
         A.value(""),
       )
       ```

       ## Event handlers

       Attach event listeners with typed callbacks:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       button(
         A.onClick(_ => println("Clicked!")),
         A.onMouseOver(_ => println("Hovered")),
         "Click me",
       )
       ```

       ## Conditional rendering

       Use `when` for conditional content:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       def statusBadge(isActive: Boolean) = div(
         when(isActive)(span(A.`class`("active"), "Online")),
         when(!isActive)(span(A.`class`("offline"), "Offline")),
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
       See the [Examples](/Examples) page for more interactive demos.
       """,
  )
end ElementDsl
