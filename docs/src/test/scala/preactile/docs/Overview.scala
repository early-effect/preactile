package preactile.docs

import specular.core.DocSpec
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
       import preactile.dsl._

       object HelloWorld extends PreactileComponent:
         type Props = Unit
         def render(props: Props) = div("Hello, world!")
       ```

       Then mount it to the DOM:

       ```scala mdoc:compile-only
       import preactile.impl.Preact

       @main def run(): Unit =
         Preact.render(HelloWorld(), document.getElementById("app"))
       ```

       See the [Components](/Components) page for more examples.
       """,
  )
