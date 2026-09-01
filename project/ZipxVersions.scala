import zipx.*

/** Typed catalog: every library and plugin this build may use. `zipxDepUpdate` rewrites constructors here.
  *
  * sbt-zipx is not a row: generate emits it from the loaded plugin (`zipxSelfPlugins`). sbt-pgp is not a row: zipx
  * already brings it in. Action pins stay on jar defaults.
  */
object ZipxVersions extends zipx.ZipxVersions:

  val sbt: SbtVersion     = SbtVersion("2.0.7")
  val scala: ScalaVersion = ScalaVersion("3.8.4")

  // Libraries
  val zio: Lib               = Lib("dev.zio", "zio", "2.1.26")
  val zioTest: Lib           = zio.mod("zio-test").test
  val zioTestSbt: Lib        = zio.mod("zio-test-sbt").test
  val scalajsDom: Lib        = Lib("org.scala-js", "scalajs-dom", "2.8.0")
  val conduit: Lib           = Lib("io.github.russwyte", "conduit", "0.0.6")
  val scalaJavaTime: Lib     = Lib("io.github.cquiroz", "scala-java-time", "2.7.0")
  val scalaJavaTimeTzdb: Lib = scalaJavaTime.mod("scala-java-time-tzdb")
  val specular: Lib          = Lib("rocks.earlyeffect", "specular-core", "0.14.1")
  val specularZioTest: Lib   = specular.mod("specular-zio-test").test
  val specularSite: Lib      = specular.mod("specular-site").test
  val specularTheme: Lib     = specular.mod("early-effect-docs-theme").test
  val chekhov: Lib           = Lib("rocks.earlyeffect", "chekhov-zio-test", "0.0.5").test
  val chekhovDriver: Lib     = chekhov.mod("chekhov-driver").test
  // DevReload client for the example (ascent-preview SSE). specular-core already pulls this for docsClient.
  val ascentJs: Lib = Lib("rocks.earlyeffect", "ascent-js", "0.5.0")
  // JVM PreviewMain, selected by the examplePreview subproject and put on ascentPreviewClasspath.
  val ascentPreview: Lib = Lib("rocks.earlyeffect", "ascent-preview", "0.5.0")

  // sbt plugins (project/plugins.sbt is generated from these rows plus zipxSelfPlugins).
  // sbt-sonatype has no sbt 2 artifact; sbt 2.x includes Sonatype Central support built-in via
  // localStaging.value and publishTo (see build.sbt). sbt-splice bundles pinned JS (Preact) into
  // the Scala.js output; sbt-ascent-preview comes in transitively from sbt-specular 0.14.1.
  val scalajsPlugin: Plugin  = Plugin("org.scala-js", "sbt-scalajs", "1.22.0")
  val scalafmt: Plugin       = Plugin("org.scalameta", "sbt-scalafmt", "2.6.2")
  val scalafix: Plugin       = Plugin("ch.epfl.scala", "sbt-scalafix", "0.14.7")
  val dynverCi: Plugin       = Plugin("rocks.earlyeffect", "sbt-dynver-ci", "0.2.3")
  val specularPlugin: Plugin = Plugin("rocks.earlyeffect", "sbt-specular", "0.14.1")
  val chekhovPlugin: Plugin  = Plugin("rocks.earlyeffect", "sbt-chekhov", "0.0.5")
  val splicePlugin: Plugin   = Plugin("rocks.earlyeffect", "sbt-splice", "0.1.0")
