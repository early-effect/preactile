package preactile.docs

import earlyeffect.docs.EarlyEffectTheme
import specular.site.*
import zio.*

import java.nio.file.{Files, Path}

object BuildSite extends DocsSite:
  def pages = Vector(
    Overview.doc,
    Components.doc,
    StatefulComponents.doc,
    ConduitIntegration.doc,
    ElementDsl.doc,
    Styling.doc,
    Mounting.doc,
    Examples.doc,
  )

  override def site = EarlyEffectTheme
    .brand(super.site)
    .copy(
      summaryMarkdown = Some("""A ScalaJS UI library built on Preact with live interactive examples."""),
      clientScript = Some("assets/client.js"),
    )

  override def layers = EarlyEffectTheme.layers

  override def afterBuild(out: Path, result: SiteOutput): Task[Unit] =
    EarlyEffectTheme.writeLogo(out) *> verifyClientBundle(out) *> classicClientScript(out) *> writeDevStamp(out)

  private def verifyClientBundle(out: Path): Task[Unit] =
    ZIO.attempt {
      val clientJs = out.resolve("assets/client.js")
      if !Files.isRegularFile(clientJs) then
        throw new RuntimeException(
          s"JS client bundle not found at $clientJs; run docs/specularSite first."
        )
    }

  /** spliceFull is a classic Closure script. Specular always emits `type="module"`, and modules are strict: Scala.js
    * `Throwable` does `this.message = …` on an `Error` subclass, which throws in strict mode and leaves demos
    * unmounted. Drop the attribute so the production bundle runs as a classic script. spliceFast still runs this way
    * (`const` at top level is legal in both).
    */
  private def classicClientScript(out: Path): Task[Unit] =
    ZIO.attempt {
      val dir = Files.newDirectoryStream(out, "*.html")
      try
        dir.forEach { p =>
          val html = Files.readString(p)
          val next = html.replaceAll("""type="module"(\s+src="[^"]*client\.js")""", "$1")
          if next != html then Files.writeString(p, next)
        }
      finally dir.close()
    }.unit

  /** Stamp file watched by ascent-preview after each rebuild. Harmless on CI/Pages: SpecularClient only subscribes to
    * `/__ascent/reload` on localhost.
    */
  private def writeDevStamp(out: Path): Task[Unit] =
    ZIO.attempt {
      val assets = out.resolve("assets")
      Files.createDirectories(assets)
      Files.writeString(assets.resolve("dev-stamp"), java.lang.System.currentTimeMillis.toString)
    }.unit
end BuildSite
