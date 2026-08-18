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
       - **Interoperable**: Drop in alongside any JS library or framework.

       ## Live demo

       This is a real Preactile component rendered live in your browser:
    """,
    exampleDom("overview-greeting").fromSource(
      "docs/client/src/main/scala/preactile/docs/OverviewDemo.scala",
      "demo",
    ),
    md"""
       ## Installation

       Add the dependency to your `build.sbt`:

       ```scala
       libraryDependencies += "rocks.earlyeffect" %% "preactile" % "@VERSION@"
       ```

       For conduit component support:

       ```scala
       libraryDependencies += "rocks.earlyeffect" %% "preactile-conduit" % "@VERSION@"
       ```

       ## Quick start

       Define a simple stateless component:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       object HelloWorld extends Component[Unit]:
         def render(props: Unit): VNode = div("Hello, world!")
       ```

       Then mount it to the DOM (see [Mounting](/Mounting) for setup details):

       ```scala mdoc:compile-only
       import org.scalajs.dom.document

       @main def run(): Unit =
         preactile.preact.render(HelloWorld(()), document.getElementById("app"))
       ```

       See the [Components](/Components) page for more examples.
       """,
  )
end Overview
