package preactile.docs

import earlyeffect.theme.EarlyEffectTheme
import specular.site.{DocsSite, SiteBuildResult}
import zio.*

import java.nio.file.{Files, Path, Paths, StandardCopyOption}

object BuildSite extends DocsSite:
  def pages = Vector(
    Overview.doc,
    Components.doc,
    StatefulComponents.doc,
    ElementDsl.doc,
    Styling.doc,
    Examples.doc,
  )

  override def site = EarlyEffectTheme.brand(super.site).copy(
    summaryMarkdown = Some("""A ScalaJS UI library built on Preact with live interactive examples."""),
    clientScript = Some("assets/client.js"),
  )

  override def layers = EarlyEffectTheme.layers

  override def afterBuild(out: Path, result: SiteBuildResult) =
    ZIO.succeed(EarlyEffectTheme.writeLogo(out)) *> copyClientBundle(out)

  private def copyClientBundle(out: Path): Task[Unit] =
    ZIO.attempt {
      val dest = out.resolve("assets/client.js")
      val src = findClientJs.getOrElse {
        throw new RuntimeException(
          "JS client not linked; run docs/specularSite (or docsClient/fastLinkJS) first. " +
            s"Looked for marker ${clientJsMarker}"
        )
      }
      Files.createDirectories(dest.getParent)
      Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
    }

  private def clientJsMarker: Path = Paths.get("").toAbsolutePath.resolve("target/specular-client-js.path")

  private def findClientJs: Option[Path] =
    val marker = clientJsMarker
    if !Files.isRegularFile(marker) then None
    else {
      val line = Files.readString(marker).trim
      if line.isEmpty then None
      else {
        val path = Paths.get(line)
        Option.when(Files.isRegularFile(path))(path)
      }
    }
