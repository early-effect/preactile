package preactile.docs

import specular.core.DocSpec
import specular.ziotest.DocSpecSuite

object ElementDsl extends DocSpecSuite:
  def doc = page("Element DSL")(
    md"""
       # Element DSL

       Preactile provides a type-safe DSL for building HTML elements with props and events.

       ## Basic elements

       All standard HTML elements are available as functions:

       ```scala mdoc:compile-only
       import preactile.dsl._

       div(
         h1("Title"),
         p("A paragraph of text."),
         a(href := "https://preactjs.com", "Preact homepage"),
       )
       ```

       ## Attributes and props

       Use `:=` to set element attributes:

       ```scala mdoc:compile-only
       import preactile.dsl._

       input(
         type_ := "text",
         placeholder := "Enter your name...",
         value := "",
       )
       ```

       ## Event handlers

       Attach event listeners with typed callbacks:

       ```scala mdoc:compile-only
       import preactile.dsl._

       button(
         onclick := ((event) => println("Clicked!")),
         onmouseover := ((event) => println("Hovered")),
         "Click me",
       )
       ```

       ## Conditional rendering

       Use `When` for conditional content:

       ```scala mdoc:compile-only
       import preactile.dsl._
       import preactile.When

       def statusBadge(isActive: Boolean) = div(
         When(isActive)(span(`class` := "active", "Online")),
         When(!isActive)(span(`class` := "offline", "Offline")),
       )
       ```

       See the [Examples](/Examples) page for live demos of conditional rendering and event handling.
       """,
  )
