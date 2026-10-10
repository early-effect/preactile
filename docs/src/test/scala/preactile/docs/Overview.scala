package preactile.docs

import ascent.ast.{Attr, UI}
import ascent.domtypes.AttrValue
import mermoid.{Mermaid, SvgNode, SvgRenderer}
import specular.*
import specular.ziotest.DocSpecSuite

object Overview extends DocSpecSuite:

  /** The calls `preactile.preact.render` actually makes: a Scala.js VNode, then Preact, then the DOM. */
  private val facade: Mermaid =
    Mermaid("""flowchart TD
              |  vnode["Scala.js VNode"] --> render["preactile.preact.render"]
              |  render --> host["Preact.render"]
              |  host --> dom["DOM"]
              |""".stripMargin)

  def doc = page("Overview")(
    md"""
# Preactile

Preactile is a Scala.js facade over Preact for a page that is already Preact, or that has to embed in a React 18 or Preact tree. It is not Ascent, the effect-native UI with no virtual DOM.
""",
    illustration(diagram(SvgRenderer.renderTree(facade.diagram))),
    md"""
## Live demo

A `StatefulComponent` mounted into this page. The buttons call `setState`.
""",
    exampleDom("overview-counter").fromSource(
      "docs/client/src/main/scala/preactile/docs/CounterDemo.scala",
      "demo",
    ),
    md"""
## Install

These are Scala.js artifacts. On a `ScalaJSPlugin` project, `%%` is the coordinate: sbt 2 appends `_sjs1`.

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile" % "0.0.2"
```

For conduit component support:

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile-conduit" % "0.0.2"
```
""",
    md"""
## Element DSL

The counter is a `StatefulComponent`. It builds its DOM with `E`, which is this object. `Component` takes a type parameter, so the cite is `Elements` rather than that trait.
""",
    cite[preactile.dsl.Elements.type].definition,
  )

  /** Specular has no raw-HTML node. The SVG tree is the illustration. */
  private def diagram(node: SvgNode): UI[Any] =
    svgUi(node) match
      case UI.Element(tag, attrs, children) if tag == "svg" =>
        UI.Element(
          tag,
          attrs :+ Attr.StaticAttr("style", AttrValue.Str("max-width:100%;height:auto")),
          children,
        )
      case other => other

  private def svgUi(node: SvgNode): UI[Any] = node match
    case SvgNode.Text(value)  => UI.Text(value)
    case SvgNode.Raw(content) => UI.Text(content)
    case SvgNode.Element(tag, attrs, children) =>
      UI.Element(
        tag,
        attrs.map((name, value) => Attr.StaticAttr(name, AttrValue.Str(value))).toVector,
        children.map(svgUi).toVector,
      )
end Overview
