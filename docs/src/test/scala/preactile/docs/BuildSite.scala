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
    EarlyEffectTheme.writeLogo(out) *> verifyClientBundle(out) *> writeDevStamp(out)

  private def verifyClientBundle(out: Path): Task[Unit] =
    ZIO.attempt {
      val clientJs = out.resolve("assets/client.js")
      if !Files.isRegularFile(clientJs) then
        throw new RuntimeException(
          s"JS client bundle not found at $clientJs; run docs/specularSite first."
        )
    }

  /** Stamp file polled by [[preactile.docs.DevReload]] after each rebuild (fastLink → vite → site). Harmless on
    * CI/Pages: the client only polls on localhost.
    */
  private def writeDevStamp(out: Path): Task[Unit] =
    ZIO.attempt {
      val assets = out.resolve("assets")
      Files.createDirectories(assets)
      Files.writeString(assets.resolve("dev-stamp"), java.lang.System.currentTimeMillis.toString)
    }.unit
end BuildSite
