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
    Embedding.doc,
    EmbeddingReact.doc,
    EmbeddingPreact.doc,
    Examples.doc,
  )

  override def site =
    val m = meta
    EarlyEffectTheme
      .brand(super.site)
      .copy(
        summaryMarkdown = Some("""A ScalaJS UI library built on Preact with live interactive examples."""),
        clientScript = Some("assets/client.js"),
        installSnippets = Vector(
          CodeSnippet(
            "Install",
            s"""libraryDependencies += "${m.organization}" %% "preactile" % "${m.docsVersion}"""",
          ),
          CodeSnippet(
            "Conduit components",
            s"""libraryDependencies += "${m.organization}" %% "preactile-conduit" % "${m.docsVersion}"""",
          ),
        ),
      )
  end site

  override def layers = EarlyEffectTheme.layers

  override def afterBuild(out: Path, result: SiteOutput): Task[Unit] =
    EarlyEffectTheme.writeLogo(out) *> verifyClientBundle(out) *> classicClientScript(out) *>
      injectReactHost(out) *> writeDevStamp(out)

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

  /** React 18 UMD on the Embedding in React page only. Copied from the sha256 pin cache filled by
    * `embed/embedStageHost`.
    */
  private def injectReactHost(out: Path): Task[Unit] =
    ZIO.attempt {
      val htmlPath = out.resolve("embedding-in-react.html")
      if Files.isRegularFile(htmlPath) then
        val pins = out.getParent.resolve("embed-host-pins")
        val dest = out.resolve("assets").resolve("vendor")
        Files.createDirectories(dest)
        Seq("react.development.js", "react-dom.development.js").foreach { name =>
          val src = pins.resolve(name)
          if !Files.isRegularFile(src) then
            throw new RuntimeException(s"React host pin missing at $src; run embed/embedStageHost first.")
          Files.copy(src, dest.resolve(name), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
        val html = Files.readString(htmlPath)
        val tag =
          """<script src="assets/vendor/react.development.js"></script>
<script src="assets/vendor/react-dom.development.js"></script>
"""
        if !html.contains("react.development.js") then
          val next =
            if html.contains("<head>") then html.replace("<head>", "<head>\n" + tag)
            else tag + html
          Files.writeString(htmlPath, next)
      end if
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
