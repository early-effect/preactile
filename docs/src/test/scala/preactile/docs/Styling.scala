package preactile.docs

import specular.core.DocSpec
import specular.ziotest.DocSpecSuite

object Styling extends DocSpecSuite:
  def doc = page("Styling")(
    md"""
       # Styling

       Preactile supports multiple approaches to styling your components.

       ## CSS classes

       The simplest approach is using standard CSS class names:

       ```scala mdoc:compile-only
       import preactile.dsl._

       div(
         `class` := "card",
         h2(`class` := "card-title", "My Card"),
         p(`class` := "card-body", "Card content here."),
       )
       ```

       ## StyledElement

       Use `StyledElement` for inline styles with type safety:

       ```scala mdoc:compile-only
       import preactile.dsl._
       import preactile.StyledElement

       object HighlightedBox extends PreactileComponent:
         type Props = Unit
         def render(props: Props) = StyledElement.div(
           style := Map("background" -> "yellow", "padding" -> "1rem"),
           "This box is highlighted!",
         )
       ```

       ## CSS-in-JS utilities

       Preactile includes a small CSS utility DSL for generating scoped class names:

       ```scala mdoc:compile-only
       import preactile.dsl.css._

       val cardStyle = ds(
         "background" -> "#fff",
         "borderRadius" -> "8px",
         "boxShadow" -> "0 2px 4px rgba(0,0,0,0.1)",
       )
       ```

       See the [Examples](/Examples) page for live demos of styled components.
       """,
  )
