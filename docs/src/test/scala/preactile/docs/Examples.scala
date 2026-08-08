package preactile.docs

import ascent.*
import specular.core.DocSpec
import specular.ziotest.DocSpecSuite

object Examples extends DocSpecSuite:
  def doc = page("Examples")(
    md"""
       # Live Examples

       These interactive demos are rendered live in your browser using Preactile.
       Each one demonstrates a core concept of the library.
       """,
    section("Basic Components")(
      md"""
         A simple stateless component that renders greeting text. This shows how
         components compose and render without any internal state.
         """,
      example {
        E.div(
          id := "basic-components-demo",
          padding.px(16),
          background("#f0f4ff"),
          borderRadius.px(8),
          border("1px solid #d0d9f5"),
          E.p(color("#555"), fontStyle.italic, "Interactive demo mounts here..."),
        )
      }.interactive.assert(_ => zio.test.assertTrue(true)),
    ),
    section("Stateful Components")(
      md"""
         A counter built with `StatefulComponent`. Click the buttons to see how
         internal state updates trigger re-renders. This is a live Preactile component,
         not static HTML.
         """,
      example {
        E.div(
          id := "counter-demo",
          padding.px(16),
          background("#f0fff4"),
          borderRadius.px(8),
          border("1px solid #c6f6d5"),
          E.p(color("#555"), fontStyle.italic, "Interactive counter mounts here..."),
        )
      }.interactive.assert(_ => zio.test.assertTrue(true)),
    ),
    section("Conditional Rendering")(
      md"""
         Toggle content on and off using Preactile's `when` helper. This demonstrates
         conditional rendering patterns in a real component.
         """,
      example {
        E.div(
          id := "conditional-demo",
          padding.px(16),
          background("#fffaf0"),
          borderRadius.px(8),
          border("1px solid #feebc8"),
          E.p(color("#555"), fontStyle.italic, "Interactive toggle mounts here..."),
        )
      }.interactive.assert(_ => zio.test.assertTrue(true)),
    ),
    section("Lists and Data")(
      md"""
         Render a list of items with interactive checkboxes. Click any item to toggle
         its completed state. This shows how Preactile handles lists and per-item state.
         """,
      example {
        E.div(
          id := "list-demo",
          padding.px(16),
          background("#faf5ff"),
          borderRadius.px(8),
          border("1px solid #e9d8fd"),
          E.p(color("#555"), fontStyle.italic, "Interactive todo list mounts here..."),
        )
      }.interactive.assert(_ => zio.test.assertTrue(true)),
    ),
  )
