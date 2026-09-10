package preactile.docs

import chekhov.*
import chekhov.driver.PlaywrightDriver
import zio.*
import zio.test.*

import java.nio.file.Path

/** Chromium E2E for host React 18 and host Preact pages staged by embed/ascentPreviewStage. */
object EmbedHostSpec extends ZIOSpecDefault:

  private val previewDir =
    Path.of("..", "embed", "target", "preview").toAbsolutePath.normalize()

  override def aspects =
    Chunk(
      TestAspect.withLiveClock,
      TestAspect.timeout(30.seconds),
      TestAspect.sequential,
    )

  private def waitForTrue(page: Page, expression: String, timeoutMsg: String): IO[ChekhovError, Unit] =
    def attempt: IO[ChekhovError, Boolean] =
      page.evaluate(expression, isFunction = true).map(_.contains("\"b\":true"))
    def loop: IO[ChekhovError, Unit] =
      attempt.flatMap {
        case true  => ZIO.unit
        case false => ZIO.sleep(150.millis) *> loop
      }
    loop.timeoutFail(ChekhovError.Timeout(timeoutMsg))(8.seconds)
  end waitForTrue

  private def waitMounted(page: Page, chrome: String): IO[ChekhovError, Unit] =
    waitForTrue(
      page,
      s"""() => (document.getElementById('host-chrome')?.textContent || '') === '$chrome'""",
      s"embed host '$chrome' did not mount",
    )

  private def config =
    ChekhovConfig(
      browser = ChekhovBrowser.Chromium,
      headless = true,
      traceCapture = ArtifactCapture.OnFailure,
      videoCapture = ArtifactCapture.Off,
    )

  def spec = suite("Embed hosts")(
    test("React 18 host mounts Preactile greeting, stateful click, and conduit"):
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/")
        _      <- waitMounted(page, "host-react")
        chrome <- page.innerText("#host-chrome")
        hello  <- page.innerText("#greeting")
        _      <- page.click("#stateful")
        _ <- waitForTrue(
          page,
          """() => (document.getElementById('stateful')?.textContent || '') === 'state:1'""",
          "stateful counter did not increment",
        )
        _ <- page.click("#conduit")
        _ <- waitForTrue(
          page,
          """() => (document.getElementById('conduit')?.textContent || '') === 'count:1'""",
          "conduit counter did not increment",
        )
        state <- page.innerText("#stateful")
        count <- page.innerText("#conduit")
      yield assertTrue(
        chrome == "host-react",
        hello == "from-preactile",
        state == "state:1",
        count == "count:1",
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(previewDir),
        PlaywrightDriver.suiteLayers,
      )
    ,
    test("host Preact mounts Preactile greeting, stateful click, and conduit"):
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/preact.html")
        _      <- waitMounted(page, "host-preact")
        chrome <- page.innerText("#host-chrome")
        hello  <- page.innerText("#greeting")
        _      <- page.click("#stateful")
        _ <- waitForTrue(
          page,
          """() => (document.getElementById('stateful')?.textContent || '') === 'state:1'""",
          "stateful counter did not increment",
        )
        _ <- page.click("#conduit")
        _ <- waitForTrue(
          page,
          """() => (document.getElementById('conduit')?.textContent || '') === 'count:1'""",
          "conduit counter did not increment",
        )
        state <- page.innerText("#stateful")
        count <- page.innerText("#conduit")
      yield assertTrue(
        chrome == "host-preact",
        hello == "from-preactile",
        state == "state:1",
        count == "count:1",
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(previewDir),
        PlaywrightDriver.suiteLayers,
      ),
  )
end EmbedHostSpec
