package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Overview extends DocSpecSuite:
  def doc = page("Overview")(
    md"""
# Preactile

A ScalaJS UI library built on [Preact](https://preactjs.com/), bringing
component-based reactive programming to the JVM ecosystem.

## Why Preactile?

- **Familiar**: If you know React or Preact, the mental model transfers directly.
- **Lightweight**: Preact's tiny footprint (3KB gzipped) keeps your bundles small.
- **Type-safe**: Full Scala 3 type safety for components, props, and events.
- **Interoperable**: Embed in a host React 18 or Preact tree; see [Embedding](/Embedding).

## Live demo

This is a real Preactile component rendered live in your browser:
""",
    exampleDom("overview-greeting").fromSource(
      "docs/client/src/main/scala/preactile/docs/OverviewDemo.scala",
      "demo",
    ),
    md"""
## Installation

Add the dependency to your `build.sbt`. These are Scala.js artifacts; `%%` is enough
on a `ScalaJSPlugin` project (sbt 2 already appends `_sjs1`):

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile" % "<version>"
```

For conduit component support:

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile-conduit" % "<version>"
```

The index page shows the current published version.

## Quick start

Define a simple stateless component:

```scala
import preactile.*

object HelloWorld extends Component[Unit]:
  def render(props: Unit): VNode = E.div("Hello, world!")
```

Then mount it to the DOM (see [Mounting](/Mounting) for setup details):

```scala
import org.scalajs.dom.document

@main def run(): Unit =
  preactile.preact.render(HelloWorld(()), document.getElementById("app"))
```

See the [Components](/Components) page for more examples.
""",
  )
end Overview
