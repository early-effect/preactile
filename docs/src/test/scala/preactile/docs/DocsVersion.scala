package preactile.docs

import zio.test.*

/** Last published tag. A bare next version is not on Central. */
object DocsVersion:

  /** Last published tag. Update this in the release commit. */
  val published = "0.0.2"

  /** Returns [[published]]. A bare next version, `-ci`, `+`, and `SNAPSHOT` are unpublished. */
  def advertise(raw: String): String =
    if raw.trim == published then published else published
end DocsVersion

object DocsVersionSpec extends ZIOSpecDefault:
  def spec = suite("DocsVersion")(
    test("advertise the published tag") {
      assertTrue(
        DocsVersion.advertise("0.0.3") == DocsVersion.published,
        DocsVersion.advertise("0.0.3-ci") == DocsVersion.published,
        DocsVersion.advertise("0.0.2+1-abc") == DocsVersion.published,
        DocsVersion.advertise(DocsVersion.published) == DocsVersion.published,
      )
    }
  )
end DocsVersionSpec
