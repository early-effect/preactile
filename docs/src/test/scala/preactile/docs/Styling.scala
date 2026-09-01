package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Styling extends DocSpecSuite:
  def doc = page("Styling")(
    md"""
# Styling

Preactile supports a few complementary approaches to styling.

## CSS classes

The simplest approach is a standard class name:

```scala
import preactile.*

E.div(
  A.`class`("card"),
  E.h2(A.`class`("card-title"), "My Card"),
  E.p(A.`class`("card-body"), "Card content here."),
)
```

## Inline styles

`A.style` takes CSS `Declaration`s from `S`, not a `Map`:

```scala
import preactile.*

E.div(
  A.style(S.background("yellow"), S.padding("1rem")),
  "This box is highlighted!",
)
```

## CSS-in-JS (`CssClass`)

`preactile.dsl.css.CssClass` generates a scoped class and injects the rules into the
document. Use it for styles that belong to a component:

```scala
import preactile.*
import preactile.dsl.css.CssClass

object CardStyles extends CssClass(
  S.background("#fff"),
  S.borderRadius("8px"),
  S.boxShadow("0 2px 4px rgba(0,0,0,0.1)"),
)
```

`StyledElement` is a `CssClass` that also constructs a specific HTML tag, so you can write
`object Card extends StyledElement(E.div)(S.padding("1rem"))` and then `Card("…")`.

## Live demo: CSS-in-JS and inline styles

This card uses `CssClass` for scoped styling plus inline styles for one-off overrides:
""",
    exampleDom("styling-demo").fromSource(
      "docs/client/src/main/scala/preactile/docs/StylingDemo.scala",
      "demo",
    ),
    md"""
The `CardStyles`, `TitleStyles`, and `BodyStyles` objects compose CSS rules that are scoped
to the component. Inline styles via `A.style(S.marginTop("12px"), …)` handle quick overrides
without defining a separate style object.

See the [Examples](/Examples) page for the other live demos.
""",
  )
end Styling
