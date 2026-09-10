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
// The CI test job still needs Node: Chekhov E2E drives Chromium through the pinned Playwright
// CLI (docs/chekhovInstall), and core's JSDOM tests need core/node_modules (jsdom, preact) via
// NODE_PATH. The docs site itself is Node-free (sbt-splice). Browsers land in
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
      embed / embedStageHost,
      testFull,
      embed / embedDceCheck,
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

// sbt-splice: shared Preact pin. spliceLibs defaults to empty (splice is inert on the published
// core/conduit projects); docsClient and example splice this pinned module into the Scala.js
// output instead of bundling with Node. CDN pins require the sha256 — an unresolved or mismatched
// specifier fails the splice task. spliceFull (0.1.0+) keeps JS property names, so Preact class
// components need no extra keep-list.
val preactSplice = Splice.lib("preact", "10.26.4", "dist/preact.module.js")
  .sha256("2ce1b7b810fc14cda3f4242e636311b5a5e6d5a6cb1f3274fe948fe3bba3d32e")

val splicePreactSettings = Def.settings(
  spliceResolvers += Splice.jsDelivr,
  spliceLibs      ++= Seq(preactSplice),
)

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
  .aggregate(core, preactileConduit, docs, docsClient, example, examplePreview, embed)
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
    // SplicePlugin (allRequirements) auto-enables here, but spliceLibs stays at the default
    // (empty): core is a published library — consumers splice their own pinned JS.
    // Preact is imported via @JSImport("preact"). JSDOM tests still resolve it from
    // core/node_modules (NODE_PATH). JSDOMNodeJSEnv is vendored in
    // project/JSDOMNodeJSEnv.scala (scalajs-env-jsdom-nodejs has no sbt 2 artifact).
    Test / jsEnv := Def.uncached { new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv() },
    // Make core/node_modules visible to Node.js when running tests from project root
    // (jsdom, preact, and react / react-dom for host-embed tests).
    Test / envVars += ("NODE_PATH" -> (baseDirectory.value / "node_modules").getAbsolutePath),
    // Tests use CommonJS so vm.runInThisContext can execute them (no dynamic import needed).
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= V.deps(V.zio.test, V.zioTest, V.zioTestSbt, V.scalajsDom),
  )

lazy val preactileConduit = project
  .in(file("conduit"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core % "compile->compile;test->test")
  .settings(
    name := "preactile-conduit",
    // Published library: spliceLibs stays at the default (empty), so splice is inert here.
    Test / fork := false, // ChekhovPlugin forces fork := true; Scala.js tests must not fork
    Test / jsEnv := Def.uncached { new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv() },
    Test / envVars += ("NODE_PATH" -> ((ThisBuild / baseDirectory).value / "core" / "node_modules").getAbsolutePath),
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= V.deps(
      V.conduit,
      V.scalaJavaTime,
      V.scalaJavaTimeTzdb,
      V.zio.test,
      V.zioTest,
      V.zioTestSbt,
      V.scalajsDom,
    ),
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
    // CI docs builds are dynver `-ci`; stripCi drops the suffix so install snippets and chrome
    // show the last published tag instead of advertising 0.x.y-ci.
    specularDisplayVersion := stripCi,
    // Production site: Closure-advanced spliced client (JDK 21+). spliceFull downloads the pinned
    // preact from jsDelivr (sha256-verified), remaps @JSImport("preact", …) into the bundle, and
    // Closures it — no Node. Runs before specularBuildMain, so create the assets dir ourselves;
    // BuildSite.afterBuild then verifies assets/client.js exists.
    specularJsLink := Def.uncached {
      val js   = (docsClient / spliceFull).value
      val dest = specularSiteDirectory.value / "assets" / "client.js"
      IO.createDirectory(dest.getParentFile)
      IO.copyFile(js, dest)
    },
    // Dev loop: fast spliced client. `sbt ~docs/specularPreview` restages via specularSiteDev
    // and serves target/site with ascent-preview (SSE reload on assets/dev-stamp).
    specularJsLinkDev := Def.uncached {
      val js   = (docsClient / spliceFast).value
      val dest = specularSiteDirectory.value / "assets" / "client.js"
      IO.createDirectory(dest.getParentFile)
      IO.copyFile(js, dest)
    },
    // Chekhov E2E tests: fork so the Playwright driver can spawn node. The driver CLI is the
    // pinned Playwright installed by `docs/chekhovInstall`, auto-discovered from the Chekhov
    // cache; in CI browsers come from PLAYWRIGHT_BROWSERS_PATH (zipxEnv above).
    Test / fork := true,
    Test / resourceGenerators += Def.task {
      val _ = (embed / embedStageHost).value
      Seq.empty[File]
    }.taskValue,
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
    splicePreactSettings,
    libraryDependencies ++= V.deps(V.zio, V.specular),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    Compile / mainClass := Some("preactile.docs.ClientMain"),
  )

lazy val example = project
  .in(file("example"))
  .enablePlugins(ScalaJSPlugin, AscentPreviewPlugin)
  .dependsOn(core, preactileConduit)
  .settings(
    name                            := "preactile-example",
    Test / fork                     := false, // ChekhovPlugin forces fork := true; Scala.js tests must not fork
    publish / skip                  := true,
    test / skip                     := true,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    splicePreactSettings,
    // Stage the fast spliced bundle next to index.html; `sbt ~example/ascentPreview` serves it
    // with SSE reload. Port 8766 so it does not collide with docs on 8765.
    spliceFastOutput         := Def.uncached(ascentPreviewRoot.value / "fast.js"),
    ascentPreviewPort        := AscentPreviewPort(8766),
    ascentPreviewClasspath   := Def.uncached((LocalProject("examplePreview") / Compile / fullClasspath).value),
    libraryDependencies ++= V.deps(V.ascentJs),
  )

// JVM classpath for PreviewMain. AscentPreviewPlugin.ascentPreviewLibVersion injects
// `ascent-preview_3` via `%`, which zipxCheckDeps cannot match to a Binary Lib row.
lazy val examplePreview = project
  .in(file("example/preview"))
  .settings(
    name           := "preactile-example-preview",
    publish / skip := true,
    test / skip    := true,
    libraryDependencies ++= V.deps(V.ascentPreview),
  )

lazy val embedDceCheck  = taskKey[Unit]("Fail if the embed bundle imports preact")
lazy val embedStageHost = taskKey[File]("Stage React/Preact host pages next to the embed bundle")

// Unpublished Scala.js fixture: Host.useReact / Host.usePreactHost, no spliceLibs.
// Host UMD scripts are fetched with sha256 into the preview tree, not spliced in.
lazy val embed = project
  .in(file("embed"))
  .enablePlugins(ScalaJSPlugin, AscentPreviewPlugin)
  .dependsOn(core, preactileConduit)
  .settings(
    name           := "preactile-embed",
    publish / skip := true,
    Test / fork    := false,
    // ES-module DCE fixture. Disable the Test config so root testFull does not
    // discover frameworks and Node-execute the bundle as CJS.
    Test / loadedTestFrameworks := Def.uncached(Map.empty),
    Test / definedTests         := Def.uncached(Nil),
    Test / definedTestNames     := Def.uncached(Nil),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    Compile / mainClass             := Some("preactile.embed.Main"),
    spliceFastOutput                := Def.uncached(ascentPreviewRoot.value / "fast.js"),
    ascentPreviewPort               := AscentPreviewPort(8767),
    ascentPreviewClasspath          := Def.uncached((LocalProject("examplePreview") / Compile / fullClasspath).value),
    ascentPreviewRebuild := Def.uncached {
      val _ = embedStageHost.value
      ()
    },
    embedStageHost := Def.uncached {
      val dest  = ascentPreviewStage.value
      val cache = (ThisBuild / baseDirectory).value / "target" / "embed-host-pins"
      IO.createDirectory(dest / "vendor")
      IO.createDirectory(cache)
      def pin(name: String, url: String, sha: String): Unit =
        val cached = cache / name
        val body =
          if cached.exists then IO.readBytes(cached)
          else
            val bytes = java.net.URI.create(url).toURL.openStream().readAllBytes()
            IO.write(cached, bytes)
            bytes
        val hex = java.security.MessageDigest.getInstance("SHA-256").digest(body).map(b => f"$b%02x").mkString
        if hex != sha then
          cached.delete()
          sys.error(s"embedStageHost: sha256 mismatch for $name: $hex")
        IO.copyFile(cached, dest / "vendor" / name)
      pin(
        "react.development.js",
        "https://cdn.jsdelivr.net/npm/react@18.3.1/umd/react.development.js",
        "28348fef6cb0ed8b2ceeb22deaf824428fd13875d84c73d38f77dd216fc24e7f",
      )
      pin(
        "react-dom.development.js",
        "https://cdn.jsdelivr.net/npm/react-dom@18.3.1/umd/react-dom.development.js",
        "f9044a5e9c39db8bb1a204dff924e526ec0a621e695bb69de1035811be8709e4",
      )
      pin(
        "preact.umd.js",
        "https://cdn.jsdelivr.net/npm/preact@10.26.4/dist/preact.umd.js",
        "6ba7a5946990492ba7fc40e79530a1164739f586077070e558878a51d341c0b5",
      )
      IO.copyFile(baseDirectory.value / "preact.html", dest / "preact.html")
      dest
    },
    embedDceCheck := Def.uncached {
      val js  = (Compile / spliceFast).value
      val txt = IO.read(js)
      val hits =
        txt.contains("from \"preact\"") || txt.contains("from 'preact'") ||
          txt.contains("require(\"preact\")") || txt.contains("require('preact')") ||
          txt.contains("preact.module.js") || txt.contains("preact.umd.js")
      if hits then sys.error(s"embed bundle still imports preact: ${js.getName}")
    },
  )
