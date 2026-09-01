package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Mounting extends DocSpecSuite:
  def doc = page("Mounting")(
    md"""
# Mounting

Preactile components render to the DOM via Preact's `render` function. This page covers
how to set up your project and mount components in different environments.

## Prerequisites

You need two things: a ScalaJS build that includes preactile, and Preact itself, spliced
into your bundle by [sbt-splice](https://github.com/early-effect/sbt-splice). No npm or
Node is required for bundling.

### Add sbt-splice

```scala
// project/plugins.sbt
addSbtPlugin("rocks.earlyeffect" % "sbt-splice" % "0.1.0")
```

### Pin Preact

Preact is imported via `@JSImport`, so the browser needs a copy of it. Splice downloads a
pinned copy from a CDN and inlines it into your bundle. The sha256 makes the pin
verifiable: an unresolved or mismatched specifier fails the splice task.

```scala
val preactSplice = Splice.lib("preact", "10.26.4", "dist/preact.module.js")
  .sha256("2ce1b7b810fc14cda3f4242e636311b5a5e6d5a6cb1f3274fe948fe3bba3d32e")

lazy val example = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    spliceResolvers += Splice.jsDelivr,
    spliceLibs      ++= Seq(preactSplice),
  )
```

## Building the bundle

Splice produces a self-contained ES module that your page loads directly:

- **Development** — `sbt example/spliceFast` writes an unminified bundle with source maps
  to `example/target/splice/fast.js`.
- **Production** — `sbt example/spliceFull` Closure-optimizes the same bundle (JDK 21+)
  to `example/target/splice/full.js`.

Your `index.html` loads the bundle and provides a mount point:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Preactile App</title>
  <script type="module" src="/fast.js"></script>
</head>
<body>
  <div id="app"></div>
</body>
</html>
```

Serve the output directory with any static file server.

### Development workflow

This repo's example is:

```bash
sbt ~example/ascentPreview
```

That stages the spliced bundle and serves it with
[ascent-preview](https://github.com/early-effect/ascent) at `http://localhost:8766`.
Rebuilds rewrite `assets/dev-stamp`; the app opts in with `ascent.js.DevReload.install()`
(localhost only). Docs use the same loop: `sbt ~docs/specularPreview`.

## Minimal mount code

Regardless of your build setup, mounting a component looks the same:

```scala
import org.scalajs.dom.document
import preactile.*

object App extends Component[Unit]:
  def render(props: Unit): VNode =
    E.div(
      E.h1("My Preactile App"),
      E.p("Hello from ScalaJS!"),
    )

@main def run(): Unit =
  val root = document.getElementById("app")
  preactile.preact.render(App(()), root)
```

The `@main` annotation makes this your entry point when using
`scalaJSUseMainModuleInitializer := true`.

## Mounting multiple components

You can mount different components into different DOM elements:

```scala
import org.scalajs.dom.document
import preactile.*

object Header extends Component[Unit]:
  def render(props: Unit): VNode = E.header(E.h1("Header"))

object MainContent extends Component[Unit]:
  def render(props: Unit): VNode = E.main(E.p("Main content here."))

@main def run(): Unit =
  preactile.preact.render(Header(()), document.getElementById("header"))
  preactile.preact.render(MainContent(()), document.getElementById("main"))
```

## Unmounting

To remove a component from the DOM, pass `null` to render:

```scala
import org.scalajs.dom.document
import preactile.*

def unmount(elementId: String): Unit =
  val el = document.getElementById(elementId)
  if el != null then preactile.preact.render(null, el)
```

This triggers `componentWillUnmount` lifecycle hooks and cleans up subscriptions.

## Next steps

- See [Components](/Components) for building your first component
- See [Stateful Components](/StatefulComponents) for managing internal state
- See [Conduit Integration](/ConduitIntegration) for effect-native state management
"""
  )

end Mounting
