package preactile.docs

import chekhov.*
import chekhov.driver.PlaywrightDriver
import zio.*
import zio.test.*

/** E2E tests for the Todo demo on conduit-integration.html.
  *
  * Serves target/site statically and exercises all interactive features:
  *   - Add todos via Enter key
  *   - Toggle completion checkboxes
  *   - Filter by All/Active/Completed
  *   - Clear completed button
  */
object TodoDemoSpec extends ZIOSpecDefault:

  // Site is built at repo root target/site, not docs/target/site.
  // sbt runs tests from the module directory, so go up one level.
  private val siteDir = java.nio.file.Paths.get("..", "target", "site").toAbsolutePath.normalize()

  override def aspects =
    Chunk(
      TestAspect.withLiveClock,
      TestAspect.timeout(15.seconds),
      TestAspect.sequential,
    )

  private val appContainer = "[data-specular-mount='conduit-todo']"

  /** Wait for the Todo demo SPA to mount and render its initial state. */
  private def waitForTodoApp(page: Page)(using Trace): IO[ChekhovError, Unit] =
    page
      .evaluate(
        """() => {
        const el = document.querySelector('[data-specular-mount="conduit-todo"]');
        return !!el && !el.querySelector('.specular-dom-fallback');
      }""",
        isFunction = true,
      )
      .flatMap { result =>
        if result.contains("\"b\":true") then ZIO.unit
        else ZIO.fail(ChekhovError.Protocol("Todo app not yet mounted"))
      }
      .retry(Schedule.recurs(6) && Schedule.spaced(150.millis))
      .timeoutFail(ChekhovError.Timeout("Todo app did not mount within 2s"))(2.seconds)

  def spec = suite("TodoDemo")(
    test("renders empty state on load") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        text   <- page.innerText(appContainer)
      yield assertTrue(text.contains("No todos yet"))).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
    test("add a todo via Enter key") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        input = "input[placeholder='What needs to be done?']"
        _    <- page.fill(input, "Buy milk")
        _    <- page.press(input, "Enter")
        text <- page.innerText(appContainer)
      yield assertTrue(
        text.contains("Buy milk"),
        text.contains("1 item left"),
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
    test("toggle completion checkbox") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        input = "input[placeholder='What needs to be done?']"
        _ <- page.fill(input, "Walk the dog")
        _ <- page.press(input, "Enter")
        // Click the first todo's checkbox - use li for todo items.
        _    <- page.click(appContainer + " ul li:first-of-type input[type='checkbox']")
        text <- page.innerText(appContainer)
      yield assertTrue(
        text.contains("0 items left")
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
    test("filter by Active hides completed todos") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        input = "input[placeholder='What needs to be done?']"
        // Add two todos.
        _ <- page.fill(input, "Active task")
        _ <- page.press(input, "Enter")
        _ <- page.fill(input, "Done task")
        _ <- page.press(input, "Enter")
        // Complete the second one - use li for todo items.
        _ <- page.click(appContainer + " ul li:nth-of-type(2) input[type='checkbox']")
        // Click Active filter button using has-text selector (same as clear completed).
        _    <- page.click("button:has-text('Active')")
        text <- page.innerText(appContainer)
      yield assertTrue(
        text.contains("Active task"),
        !text.contains("Done task"),
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
    test("clear completed removes done todos") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        input = "input[placeholder='What needs to be done?']"
        // Add and complete a todo.
        _ <- page.fill(input, "To clear")
        _ <- page.press(input, "Enter")
        // Complete the todo - use li for todo items.
        _ <- page.click(appContainer + " ul li:first-of-type input[type='checkbox']")
        // Click Clear completed button.
        _    <- page.click("button:has-text('Clear completed')")
        text <- page.innerText(appContainer)
      yield assertTrue(
        !text.contains("To clear"),
        text.contains("No todos yet"),
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
    test("filter by Completed shows only done todos") {
      val config = ChekhovConfig(
        browser = ChekhovBrowser.Chromium,
        headless = true,
        traceCapture = ArtifactCapture.OnFailure,
        videoCapture = ArtifactCapture.Off,
      )
      (for
        server <- ZIO.service[AppServer]
        page   <- ZIO.service[Page]
        _      <- page.goto(server.baseUrl + "/conduit-integration.html")
        _      <- waitForTodoApp(page)
        input = "input[placeholder='What needs to be done?']"
        // Add two todos, complete one.
        _ <- page.fill(input, "Still active")
        _ <- page.press(input, "Enter")
        _ <- page.fill(input, "Now done")
        _ <- page.press(input, "Enter")
        // Complete the second one - use li for todo items.
        _ <- page.click(appContainer + " ul li:nth-of-type(2) input[type='checkbox']")
        // Click Completed filter button - target Filters div to avoid matching "Clear completed".
        _    <- page.click("div:has-text('All') button:nth-of-type(3)")
        text <- page.innerText(appContainer)
      yield assertTrue(
        !text.contains("Still active"),
        text.contains("Now done"),
      )).provide(
        ZLayer.succeed(config),
        StaticFileServer.layer(siteDir),
        PlaywrightDriver.suiteLayers,
      )
    },
  )

end TodoDemoSpec
