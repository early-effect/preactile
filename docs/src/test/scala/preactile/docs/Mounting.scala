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

       You need two things: a ScalaJS build that includes preactile, and Preact itself as an npm dependency.

       ### Add Preact to your project

       Since Preact is imported via `@JSImport`, you must include it in your npm dependencies:

       ```bash
       npm install preact
       ```

       Or with yarn:

       ```bash
       yarn add preact
       ```

       ## Vite setup (recommended)

       The simplest approach uses Vite as your bundler. Create a `vite.config.js`:

       ```js
       import { defineConfig } from 'vite';

       export default defineConfig({
         build: {
           rollupOptions: {
             input: './public/index.html',
           },
         },
       });
       ```

       Your `index.html` loads the ScalaJS bundle and provides a mount point:

       ```html
       <!DOCTYPE html>
       <html lang="en">
       <head>
         <meta charset="UTF-8" />
         <title>Preactile App</title>
       </head>
       <body>
         <div id="app"></div>
         <script type="module" src="/target/scala-3/preactile-example-fastopt/main.js"></script>
       </body>
       </html>
       ```

       Run with `npm run dev` and point your browser to the Vite URL.

       ### Development workflow

       For live reload during development, run these in parallel:

       Terminal 1 (Vite):
       ```bash
       npm run dev
       ```

       Terminal 2 (sbt watch):
       ```bash
       sbt "~example/fastLinkJS"
       ```

       Changes to Scala code trigger a rebuild; Vite reloads the page automatically.

       ## sbt-only setup

       If you prefer not to use npm/Vite, you can bundle everything with sbt and serve static files:

       1. Include Preact via jsDependencies:

          ```scala
          lazy val example = project
            .enablePlugins(ScalaJSPlugin)
            .dependsOn(core)
            .settings(
              scalaJSUseMainModuleInitializer := true,
              jsDependencies ++= Seq(
                "org.webjars.npm" % "preact" % "10.26.4" / "dist/preact.min.js"
                  minified "dist/preact.min.module.js",
              ),
            )
          ```

       2. Link and serve:

          ```bash
          sbt example/fullLinkJS
          # Serve from target/scala-3/example-opt with any static file server
          ```

       ## Minimal mount code

       Regardless of your build setup, mounting a component looks the same:

       ```scala mdoc:compile-only
       import org.scalajs.dom.document
       import preactile.*
       import preactile.dsl._

       object App extends Component[Unit]:
         def render(props: Unit): VNode =
           div(
             h1("My Preactile App"),
             p("Hello from ScalaJS!"),
           )

       @main def run(): Unit =
         val root = document.getElementById("app")
         preactile.preact.render(App(()), root)
       ```

       The `@main` annotation makes this your entry point when using
       `scalaJSUseMainModuleInitializer := true`.

       ## Mounting multiple components

       You can mount different components into different DOM elements:

       ```scala mdoc:compile-only
       import org.scalajs.dom.document
       import preactile.*
       import preactile.dsl._

       object Header extends Component[Unit]:
         def render(props: Unit): VNode = header(h1("Header"))

       object MainContent extends Component[Unit]:
         def render(props: Unit): VNode = main(p("Main content here."))

       @main def run(): Unit =
         preactile.preact.render(Header(()), document.getElementById("header"))
         preactile.preact.render(MainContent(()), document.getElementById("main"))
       ```

       ## Unmounting

       To remove a component from the DOM, pass `null` to render:

       ```scala mdoc:compile-only
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
