package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Styling extends DocSpecSuite:
  def doc = page("Styling")(
    md"""
       # Styling

       Preactile supports multiple approaches to styling your components.

       ## CSS classes

       The simplest approach is using standard CSS class names:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       div(
         A.`class`("card"),
         h2(A.`class`("card-title"), "My Card"),
         p(A.`class`("card-body"), "Card content here."),
       )
       ```

       ## StyledElement

       Use `StyledElement` for inline styles with type safety:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       object HighlightedBox extends Component[Unit]:
         def render(props: Unit): VNode = div(
           A.style(Map("background" -> "yellow", "padding" -> "1rem")),
           "This box is highlighted!",
         )
       ```

       ## CSS-in-JS utilities

       Preactile includes a CSS utility DSL (`preactile.dsl.css`) for generating scoped styles:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl.css.CssClass

       object CardStyles extends CssClass(
         S.background("#fff"),
         S.borderRadius("8px"),
         S.boxShadow("0 2px 4px rgba(0,0,0,0.1)"),
       )
       ```

       ## Live demo: CSS-in-JS and inline styles

       This card uses `CssClass` for scoped styling plus inline styles for one-off overrides:

    """,
    exampleDom("styling-demo").fromSource(
      "docs/client/src/main/scala/preactile/docs/StylingDemo.scala",
      "demo",
    ),
    md"""
       The `CardStyles`, `TitleStyles`, and `BodyStyles` objects compose CSS rules that are scoped
       to the component. Inline styles via `A.style(Map(...))` handle quick overrides without
       defining a separate style object.

       See the [Examples](/Examples) page for more interactive demos.
       """,
  )
end Styling
