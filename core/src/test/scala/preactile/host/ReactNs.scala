package preactile.host

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("react", JSImport.Namespace)
object ReactNs extends js.Object:
  def createElement(`type`: js.Any, props: js.Any, children: js.Any*): js.Dynamic = js.native
  val Fragment: js.Any                                                            = js.native
  val Component: js.Dynamic                                                       = js.native
  val Children: js.Dynamic                                                        = js.native
  val version: String                                                             = js.native

@js.native
@JSImport("react-dom/client", JSImport.Namespace)
object ReactDomClient extends js.Object:
  def createRoot(container: org.scalajs.dom.Element): ReactRoot = js.native

@js.native
trait ReactRoot extends js.Object:
  def render(element: js.Any): Unit = js.native
  def unmount(): Unit               = js.native

object ReactAct:
  @js.native
  @JSImport("react", "act")
  def apply(callback: js.Function0[js.Any]): js.Any = js.native
