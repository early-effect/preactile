import zipx.*

/** Typed catalog: every library and sbt plugin this build uses. `zipxDepUpdate` rewrites version literals here.
  *
  * `project/plugins.sbt` and `project/build.properties` are generated from the Plugin rows and `sbt` below
  * (`sbt zipxWorkflowGenerate`); do not edit them by hand.
  */
object ZipxVersions extends zipx.ZipxVersions:

  val sbt: SbtVersion     = SbtVersion("2.0.5")
  val scala: ScalaVersion = ScalaVersion("3.8.4")

  // Libraries
  val zio: Lib               = Lib("dev.zio", "zio", "2.1.26")
  val zioTest: Lib           = zio.mod("zio-test").test
  val zioTestSbt: Lib        = zio.mod("zio-test-sbt").test
  val scalajsDom: Lib        = Lib("org.scala-js", "scalajs-dom", "2.8.0")
  val conduit: Lib           = Lib("io.github.russwyte", "conduit", "0.0.6")
  val scalaJavaTime: Lib     = Lib("io.github.cquiroz", "scala-java-time", "2.7.0")
  val scalaJavaTimeTzdb: Lib = scalaJavaTime.mod("scala-java-time-tzdb")
  val specular: Lib          = Lib("rocks.earlyeffect", "specular-core", "0.12.0")
  val specularZioTest: Lib   = specular.mod("specular-zio-test").test
  val specularSite: Lib      = specular.mod("specular-site").test
  val specularTheme: Lib     = specular.mod("early-effect-docs-theme").test
  val chekhov: Lib           = Lib("rocks.earlyeffect", "chekhov-zio-test", "0.0.2").test
  val chekhovDriver: Lib     = chekhov.mod("chekhov-driver").test
  // The example project needs CrossVersion.for3Use2_13, which the catalog Cross cannot express, so its
  // libraryDependencies line stays in build.sbt; this row exists so zipxCheckDeps sees the GAV.
  val securerandom: Lib = Lib("org.scala-js", "scalajs-java-securerandom", "1.0.0")

  // sbt plugins (project/plugins.sbt is generated from these rows).
  // sbt-sonatype has no sbt 2 artifact; sbt 2.x includes Sonatype Central support built-in via
  // localStaging.value and publishTo (see build.sbt). scalajs-bundler also has no sbt 2 artifact;
  // we use Vite-style bundling for examples instead, with @JSImport for Preact.
  val scalajsPlugin: Plugin = Plugin("org.scala-js", "sbt-scalajs", "1.22.0")
  val scalafmt: Plugin      = Plugin("org.scalameta", "sbt-scalafmt", "2.6.2")
  val scalafix: Plugin      = Plugin("ch.epfl.scala", "sbt-scalafix", "0.14.7")
  val sbtReload: Plugin     = Plugin("com.jamesward", "sbt-reload", "0.0.7")
  val dynverCi: Plugin      = Plugin("rocks.earlyeffect", "sbt-dynver-ci", "0.2.2")
  val pgp: Plugin           = Plugin("com.github.sbt", "sbt-pgp", "2.3.1")
  val zipxPlugin: Plugin    = Plugin("rocks.earlyeffect", "sbt-zipx", "0.7.1")
  val specularPlugin: Plugin = Plugin("rocks.earlyeffect", "sbt-specular", "0.12.0")
  val chekhovPlugin: Plugin  = Plugin("rocks.earlyeffect", "sbt-chekhov", "0.0.2")
