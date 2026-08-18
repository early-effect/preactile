import org.scalajs.linker.interface.ModuleSplitStyle
import scala.sys.process._
import ZipxVersions as V

// CI-only publishing: the signing key hex comes from the PGP_KEY_HEX env var (an early-effect
// org secret). Local builds use a sentinel that keeps the build loadable but fails signing loudly.
usePgpKeyHex(sys.env.getOrElse("PGP_KEY_HEX", "MISSING_KEY_HEX"))

// Typed catalog (project/ZipxVersions.scala): scalaVersion plus the zipx CI keys (catalog rows,
// pins, action rows, zipxCheckDeps). project/plugins.sbt and project/build.properties are
// generated from it; libraryDependencies below derive from its Lib rows.
V.settings

// zipx CI configuration: the default parallel Verify policy (fmt, workflow-check,
// advisories), plus tag-triggered publish and docs deploy.
zipxJavaVersion := JdkVersion("25")
zipxCapabilities += ZipxCentral.release
zipxCapabilities += ZipxDocs.pages(sbtProject = "docs")
zipxWorkflowDispatch := true
// The CI test job needs Node: docs/specularSite bundles the client with Vite (npm), the Chekhov
// E2E tests drive Chromium through the pinned Playwright CLI (docs/chekhovInstall), and core's
// JSDOM tests need core/node_modules (jsdom, preact) via NODE_PATH. Browsers land in
// target/ms-playwright so the LocalDir sbt cache carries them between runs.
zipxEnv := Map(
  "PLAYWRIGHT_BROWSERS_PATH" -> EnvValue.typed(Expr.github("workspace") ++ Expr.lit("/target/ms-playwright"))
)
zipxCapabilities += Capability
  .once(
    name = Capability.TestName,
    command = zipxTasks.session(
      docs / specularSite,
      docs / chekhovInstall,
      testFull,
    ),
  )
  .withNodeVersion(NodeVersion("24"))
  // After the setup action (JDK + sbt + Node), before the sbt command: install core's JS
  // devDeps (jsdom for the JSDOM test env, preact for @JSImport).
  .withExtraSteps(
    Steps.built("npm-core")(
      Step
        .run(Script(zipx.shell.Exec("npm", Word.lit("install"), Word.lit("--prefix"), Word.lit("core"))))
        .named("npm install (core)"),
    )
  )

/** Dev loop: build site (fastLink client + vite + HTML), start DocsServe once, then watch-rebuild. DocsServe is a
  * static file server on target/site; it does not need restarts when assets change. Watch tracks docs + docsClient via
  * specularSite → specularJsLink → docsClient/fastLinkJS. After each rebuild, BuildSite writes assets/dev-stamp; the
  * client live-reloads on localhost. Open http://127.0.0.1:8765 — Enter exits watch (server bg job stops when sbt
  * exits).
  */
addCommandAlias("docsDev", "; docs/Test/runReload; ~docs/specularSite")

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Wunused:imports",
  "-language:implicitConversions",
)

// Publishing targets the Sonatype Central Portal, built into sbt 2.x (no sbt-sonatype needed).
// Snapshots go to Central's snapshot repo; releases stage locally and are promoted by `sonaRelease`.
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
publishMavenStyle                := true
pomIncludeRepository             := { _ => false }
ThisBuild / licenses             := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage             := Some(url("https://github.com/early-effect/preactile"))
ThisBuild / organization         := "rocks.earlyeffect"
ThisBuild / organizationName     := "early-effect"
ThisBuild / organizationHomepage := Some(url("https://github.com/early-effect"))
ThisBuild / versionScheme        := Some("early-semver")
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/early-effect/preactile"),
    "scm:git@github.com:early-effect/preactile.git",
  )
)
ThisBuild / developers := List(
  Developer(
    id = "russwyte",
    name = "Russ White",
    email = "356303+russwyte@users.noreply.github.com",
    url = url("https://github.com/russwyte"),
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, preactileConduit, docs, docsClient, example)
  .settings(
    name           := "preactile-root",
    publish / skip := true,
  )

lazy val core = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "preactile",
    // ChekhovPlugin (allRequirements) sets `Test / fork := true` on every project; Scala.js
    // `test` tasks require fork := false. Re-asserted on each Scala.js project in this build.
    Test / fork := false,
    // Preact is imported via @JSImport("preact") — consumers must include it in their npm deps.
    // JSDOMNodeJSEnv vendored in project/JSDOMNodeJSEnv.scala (scalajs-env-jsdom-nodejs has no sbt 2 artifact).
    Test / jsEnv := Def.uncached { new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv() },
    // Make core/node_modules visible to Node.js when running tests from project root.
    Test / envVars += ("NODE_PATH" -> (baseDirectory.value / "node_modules").getAbsolutePath),
    // Tests use CommonJS so vm.runInThisContext can execute them (no dynamic import needed).
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= V.deps(V.zio.test, V.zioTest, V.zioTestSbt, V.scalajsDom),
  )

lazy val preactileConduit = project
  .in(file("conduit"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(
    name        := "preactile-conduit",
    Test / fork := false, // ChekhovPlugin forces fork := true; Scala.js tests must not fork
    libraryDependencies ++= V.deps(V.conduit, V.scalaJavaTime, V.scalaJavaTimeTzdb),
  )

// docs: JVM-only Specular project (DocSpecSuites run as tests, site builds on JVM).
// ascent is used only for docs rendering (SSR + interactive mount points); preactile components
// are mounted by docsClient into the same DOM ids at runtime.
lazy val docs = project
  .in(file("docs"))
  .enablePlugins(SpecularPlugin)
  .disablePlugins(ScalaJSPlugin)
  .settings(
    name           := "preactile-docs",
    publish / skip := true,
    // The E2E suite (TodoDemoSpec) drives Chromium; `docs/chekhovInstall` installs exactly this.
    chekhovBrowser := "chromium",
    // Chekhov rows are for E2E tests against the served docs site.
    libraryDependencies ++= V.deps(
      V.specular,
      V.specularZioTest,
      V.specularSite,
      V.specularTheme,
      V.chekhov,
      V.chekhovDriver,
    ),
    // Chekhov depends on zio-json 0.10.x; specular-site pulls in zio-schema-json (0.9.x).
    dependencyOverrides += "dev.zio" %% "zio-json" % "0.10.0",
    specularMetaProject              := Some(LocalProject("core")),
    specularArtifactKind             := "library",
    specularBuildMain                := "preactile.docs.BuildSite",
    specularSiteDirectory            := (ThisBuild / baseDirectory).value / "target" / "site",
    // Link JS client and bundle with Vite so bare module specifiers (preact) resolve in browser.
    specularJsLink := Def.uncached {
      val log = streams.value.log
      (docsClient / Compile / fastLinkJS).value

      val baseDir = (ThisBuild / baseDirectory).value
      val viteDir = baseDir / "docs" / "client"

      // Copy ScalaJS output to docs/client/main.js for Vite to bundle.
      val mainJs = (docsClient / Compile / fastLinkJSOutput).value / "main.js"
      IO.copyFile(mainJs, viteDir / "main.js")

      def runNpm(cmd: String): Unit = {
        log.info(s"$cmd")
        val exit = Process(cmd, viteDir).!
        if (exit != 0) sys.error(s"$cmd failed with exit code $exit")
      }

      // Ensure node_modules exists.
      if (!(viteDir / "node_modules").exists) runNpm("npm install")

      // Bundle with Vite.
      runNpm("npm run build")

      // Verify bundled output exists.
      val bundledJs = baseDir / "target" / "site" / "assets" / "client.js"
      if (!bundledJs.exists) sys.error(s"Bundled JS not found: $bundledJs")
    },
    // Preview: DocsServe serves target/site as static files (no restart needed on rebuild).
    // `docsDev` starts it once, then `~docs/specularSite` rebuilds in place; client polls dev-stamp.
    Test / mainClass       := Some("specular.site.DocsServe"),
    Test / run / mainClass := Some("specular.site.DocsServe"),
    run / fork             := true,
    run / javaOptions ++= Seq(
      "--sun-misc-unsafe-memory-access=allow",
      "--enable-native-access=ALL-UNNAMED",
    ),
    Test / runReloadArgs := {
      val siteDir = specularSiteDirectory.value
      Seq("8765", siteDir.getAbsolutePath)
    },
    // One-shot start: ensure client JS + site exist before forking DocsServe.
    // (specularSite already depends on specularJsLink → docsClient/fastLinkJS.)
    Test / runReload := (Test / runReload).dependsOn(specularSite).value,
    // Chekhov E2E tests: fork so the Playwright driver can spawn node. The driver CLI is the
    // pinned Playwright installed by `docs/chekhovInstall`, auto-discovered from the Chekhov
    // cache; in CI browsers come from PLAYWRIGHT_BROWSERS_PATH (zipxEnv above).
    Test / fork := true,
  )

// docsClient: ScalaJS project that mounts interactive examples into SSR-rendered DOM elements.
lazy val docsClient = project
  .in(file("docs/client"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core, preactileConduit)
  .settings(
    name           := "preactile-docs-client",
    Test / fork    := false, // ChekhovPlugin forces fork := true; Scala.js tests must not fork
    publish / skip := true,
    libraryDependencies ++= V.deps(V.zio, V.specular),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    Compile / mainClass := Some("preactile.docs.ClientMain"),
  )

lazy val example = project
  .in(file("example"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core, preactileConduit)
  .settings(
    name                            := "preactile-example",
    Test / fork                     := false, // ChekhovPlugin forces fork := true; Scala.js tests must not fork
    publish / skip                  := true,
    test / skip                     := true,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("todo")))
    },
    libraryDependencies += ("org.scala-js" %% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13),
  )
