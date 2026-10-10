package preactile.docs

import earlyeffect.docs.EarlyEffectTheme
import specular.site.*
import zio.*

import java.nio.file.{Files, Path, StandardCopyOption}

object BuildSite extends DocsSite:

  /** Specular writes index.html as a summary. afterBuild copies this page over that file. The sidebar lists it once. */
  private val frontPage = Overview.doc

  def pages = Vector(
    frontPage,
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

  override def site(settings: DocsSettings): SiteModel =
    val m = settings.meta
    EarlyEffectTheme
      .brand(super.site(settings))
      .copy(
        summaryMarkdown = None,
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

  override def afterBuild(out: Path, result: SiteOutput): IO[SiteError, Unit] =
    val _ = result
    EarlyEffectTheme.writeLogo(out) *> verifyClientBundle(out) *> classicClientScript(out) *>
      injectReactHost(out) *> promoteFront(out) *> writeDevStamp(out)

  private def verifyClientBundle(out: Path): IO[SiteError, Unit] =
    val clientJs = out.resolve("assets").resolve("client.js")
    ZIO.attemptBlockingIO(Files.isRegularFile(clientJs)).mapError(SiteError.FileUnreadable(clientJs, _)).flatMap {
      case true  => ZIO.unit
      case false => ZIO.fail(SiteError.MissingFile(clientJs))
    }

  /** spliceFull is a classic Closure script. Specular always emits `type="module"`, and modules are strict: Scala.js
    * `Throwable` does `this.message = …` on an `Error` subclass, which throws in strict mode and leaves demos
    * unmounted. Drop the attribute so the production bundle runs as a classic script. spliceFast still runs this way
    * (`const` at top level is legal in both).
    */
  private def classicClientScript(out: Path): IO[SiteError, Unit] =
    ZIO
      .attemptBlocking {
        val dir = Files.newDirectoryStream(out, "*.html")
        try
          dir.forEach { p =>
            val html = Files.readString(p)
            val next = html.replaceAll("""type="module"(\s+src="[^"]*client\.js")""", "$1")
            if next != html then Files.writeString(p, next)
          }
        finally dir.close()
      }
      .mapError(ioFailure(out, _))
      .unit

  /** React 18 UMD on the Embedding in React page only. Copied from the sha256 pin cache filled by
    * `embed/embedStageHost`.
    */
  private def injectReactHost(out: Path): IO[SiteError, Unit] =
    val htmlPath = out.resolve("embedding-in-react.html")
    val pins     = Option(out.getParent).fold(out.resolve("embed-host-pins"))(_.resolve("embed-host-pins"))
    val dest     = out.resolve("assets").resolve("vendor")
    val names    = List("react.development.js", "react-dom.development.js")
    ZIO.attemptBlockingIO(Files.isRegularFile(htmlPath)).mapError(SiteError.FileUnreadable(htmlPath, _)).flatMap {
      case false => ZIO.unit
      case true =>
        ZIO.foreachDiscard(names) { name =>
          val src = pins.resolve(name)
          ZIO.attemptBlockingIO(Files.isRegularFile(src)).mapError(SiteError.FileUnreadable(src, _)).flatMap {
            case false => ZIO.fail(SiteError.MissingFile(src))
            case true =>
              ZIO
                .attemptBlockingIO {
                  Files.createDirectories(dest)
                  val _ = Files.copy(src, dest.resolve(name), StandardCopyOption.REPLACE_EXISTING)
                }
                .mapError(SiteError.WriteFailed(dest.resolve(name), _))
                .unit
          }
        } *> ZIO.attemptBlockingIO(Files.readString(htmlPath)).mapError(SiteError.FileUnreadable(htmlPath, _)).flatMap {
          html =>
            if html.contains("react.development.js") then ZIO.unit
            else
              val tag =
                """<script src="assets/vendor/react.development.js"></script>
<script src="assets/vendor/react-dom.development.js"></script>
"""
              val next =
                if html.contains("<head>") then html.replace("<head>", "<head>\n" + tag)
                else tag + html
              ZIO.attemptBlockingIO(Files.writeString(htmlPath, next)).mapError(SiteError.WriteFailed(htmlPath, _)).unit
        }
    }
  end injectReactHost

  /** index.html is Specular's summary page. The front DocPage is the index. Asset paths stay site-relative. */
  private def promoteFront(out: Path): IO[SiteError, Unit] =
    val front = out.resolve(s"${frontPage.slug}.html")
    val index = out.resolve("index.html")
    ZIO.attemptBlockingIO(Files.isRegularFile(front)).mapError(SiteError.FileUnreadable(front, _)).flatMap {
      case false => ZIO.fail(SiteError.MissingFile(front))
      case true =>
        ZIO
          .attemptBlockingIO {
            val _ = Files.copy(front, index, StandardCopyOption.REPLACE_EXISTING)
          }
          .mapError(SiteError.WriteFailed(index, _))
          .unit
    }
  end promoteFront

  /** Stamp file watched by ascent-preview after each rebuild. Harmless on CI/Pages: SpecularClient only subscribes to
    * `/__ascent/reload` on localhost.
    */
  private def writeDevStamp(out: Path): IO[SiteError, Unit] =
    val assets = out.resolve("assets")
    val stamp  = assets.resolve("dev-stamp")
    ZIO
      .attemptBlockingIO {
        Files.createDirectories(assets)
        Files.writeString(stamp, java.lang.System.currentTimeMillis.toString)
      }
      .mapError(SiteError.WriteFailed(stamp, _))
      .unit
  end writeDevStamp

  /** `DirectoryStream.forEach` wraps `IOException` in `UncheckedIOException`. `FileUnreadable` only carries
    * `IOException`.
    */
  private def ioFailure(path: Path, err: Throwable): SiteError =
    err match
      case io: java.io.IOException => SiteError.FileUnreadable(path, io)
      case other                   => SiteError.FileUnreadable(path, new java.io.IOException(other))
end BuildSite
