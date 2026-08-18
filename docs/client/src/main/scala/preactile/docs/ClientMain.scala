package preactile.docs

import org.scalajs.dom

import specular.client.SpecularClient
import zio.*

/** Browser entry point for the docs site. */
object ClientMain extends ZIOAppDefault:

  val mounters = Map(
    "overview-greeting" -> OverviewDemo.mounter,
    "stateful-counter"  -> CounterDemo.mounter,
    "components-demo"   -> ComponentsDemo.mounter,
    "element-dsl-demo"  -> ElementDslDemo.mounter,
    "styling-demo"      -> StylingDemo.mounter,
    "conduit-todo"      -> TodoDemo.mounter,
  )

  def run = ZIO.scoped {
    ZIO.attempt(DevReload.install()).orDie *>
      SpecularClient.mountAll(mounters) *>
      ZIO.never
  }

end ClientMain

/** Live-reload for `sbt docsDev`: poll a stamp file written after each fastLink/site rebuild.
  *
  * Active only on localhost / 127.0.0.1. Production (GitHub Pages) never polls. Stamp path: `/assets/dev-stamp` (see
  * BuildSite.afterBuild).
  */
object DevReload:
  private val StampPath  = "/assets/dev-stamp"
  private val IntervalMs = 500
  private val LocalHosts = Set("localhost", "127.0.0.1", "[::1]")

  def install(): Unit =
    val host = dom.window.location.hostname
    if !LocalHosts.contains(host) then ()
    else
      var last: Option[String] = None
      def tick(): Unit =
        val xhr = new dom.XMLHttpRequest()
        // Cache-bust so the static server always returns the current stamp.
        xhr.open("GET", s"$StampPath?t=${java.lang.System.currentTimeMillis()}")
        xhr.onload = _ =>
          if xhr.status == 200 then
            val stamp = xhr.responseText.trim
            if stamp.nonEmpty then
              last match
                case None =>
                  last = Some(stamp)
                case Some(prev) if prev != stamp =>
                  dom.window.location.reload()
                case Some(_) =>
                  ()
        xhr.send()
      end tick
      dom.window.setInterval(() => tick(), IntervalMs)
      dom.console.info(s"[docs] live-reload polling $StampPath every ${IntervalMs}ms")
    end if
  end install
end DevReload
