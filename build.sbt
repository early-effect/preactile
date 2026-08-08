import org.scalajs.linker.interface.ModuleSplitStyle

// CI-only publishing: the signing key hex comes from the PGP_KEY_HEX env var (an early-effect
// org secret). Local builds use a sentinel that keeps the build loadable but fails signing loudly.
usePgpKeyHex(sys.env.getOrElse("PGP_KEY_HEX", "MISSING_KEY_HEX"))

val scala3Version   = "3.8.4"
val zioVersion      = "2.1.26"
val specularVersion = "0.11.0"

ThisBuild / scalaVersion := scala3Version

// zipx CI configuration
zipxJavaVersion := JdkVersion("25")
zipxTestTask    := "testFull"

val Fmt = CapabilityName("fmt")
zipxCapabilities += zipxTasks.once(Fmt, scalafmtCheckAll)
zipxCapabilities += Capability.test.copy(needsCapabilities = List(Fmt))
zipxCapabilities += ZipxCentral.release
zipxCapabilities += ZipxDocs.pages(sbtProject = "docs")
zipxWorkflowDispatch := true

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
publishMavenStyle    := true
pomIncludeRepository := { _ => false }
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
    // Preact is imported via @JSImport("preact") — consumers must include it in their npm deps.
    // JSDOMNodeJSEnv vendored in project/JSDOMNodeJSEnv.scala (scalajs-env-jsdom-nodejs has no sbt 2 artifact).
    Test / jsEnv := Def.uncached { new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv() },
    // Make core/node_modules visible to Node.js when running tests from project root.
    Test / envVars += ("NODE_PATH" -> (baseDirectory.value / "node_modules").getAbsolutePath),
    // Tests use CommonJS so vm.runInThisContext can execute them (no dynamic import needed).
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "dev.zio"      %% "zio"          % zioVersion % Test,
      "dev.zio"      %% "zio-test"     % zioVersion % Test,
      "dev.zio"      %% "zio-test-sbt" % zioVersion % Test,
      "org.scala-js" %% "scalajs-dom"  % "2.8.0",
    ),
  )

lazy val preactileConduit = project
  .in(file("conduit"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(
    name := "preactile-conduit",
    libraryDependencies ++= Seq(
      "io.github.russwyte" %% "conduit"              % "0.0.6",
      "io.github.cquiroz"  %% "scala-java-time"      % "2.7.0",
      "io.github.cquiroz"  %% "scala-java-time-tzdb" % "2.7.0",
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
    name := "preactile-docs",
    publish / skip := true,
    libraryDependencies ++= Seq(
      "rocks.earlyeffect" %% "specular-core"           % specularVersion % Test,
      "rocks.earlyeffect" %% "specular-zio-test"       % specularVersion % Test,
      "rocks.earlyeffect" %% "specular-site"           % specularVersion % Test,
      "rocks.earlyeffect" %% "early-effect-docs-theme" % specularVersion % Test,
    ),
    specularMetaProject := Some(LocalProject("core")),
    specularArtifactKind := "library",
    specularBuildMain := "preactile.docs.BuildSite",
    // Link the JS client bundle and write a marker path BuildSite copies into assets/client.js.
    specularJsLink := Def.uncached {
      (docsClient / Compile / fastLinkJS).value
      val outDir = (docsClient / Compile / fastLinkJSOutput).value
      val mainJs = outDir / "main.js"
      if !mainJs.exists then
        sys.error(
          s"Expected $mainJs after fastLinkJS; directory contains: " +
            Option(outDir.list).toSeq.flatten.mkString(", ")
        )
      val marker = (ThisBuild / baseDirectory).value / "target" / "specular-client-js.path"
      IO.write(marker, mainJs.getAbsolutePath)
      ()
    },
  )

// docsClient: ScalaJS project that mounts interactive examples into SSR-rendered DOM elements.
lazy val docsClient = project
  .in(file("docs/client"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(
    name := "preactile-docs-client",
    publish / skip := true,
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
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
    publish / skip                  := true,
    test / skip                     := true,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("todo")))
    },
    libraryDependencies += ("org.scala-js" %% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13),
  )
