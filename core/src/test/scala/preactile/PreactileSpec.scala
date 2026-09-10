package preactile

import org.scalajs.dom

import zio.*
import zio.test.*

abstract class PreactileSpec extends ZIOSpecDefault:
  def render(vn: => VNode): dom.Element =
    if Host.isInstalled && Host.kind != HostKind.Preact then Host.reset()
    preact.render(vn, parent)
    parent

  val parent: dom.Element =
    val res = dom.document.createElement("div")
    dom.document.documentElement.appendChild(res)
    res

  def check(s: String) = assertTrue(parent.innerHTML == s)
  def checkAfter(n: Int)(s: String) =
    for _ <- ZIO.sleep(n.millis)
    yield check(s)
end PreactileSpec
