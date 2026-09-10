package preactile.embed

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

import preactile.*
import preactile.host.*

import conduit.*

/** Bundle used to prove embed does not import Preact. Host React is expected on `globalThis.React`. */
object Main:

  case class Counter(count: Int) derives Optics
  sealed trait CountAction extends Action
  object CountActions:
    case object Increment extends CountAction

  private val handler = handle[Counter, CountAction]:
    case CountActions.Increment => update(m => m.copy(count = m.count + 1))

  private val counterConduit = Conduit.make(Counter(0))(handler)

  object Greeting extends Component[String]:
    def render(props: String): VNode = E.div(A.`class`("hi"), props)

  val CounterView = counterConduit.component { model =>
    E.button(
      A.onClick { _ =>
        counterConduit.unsafe(CountActions.Increment)
        counterConduit.unsafe.run(true)
      },
      s"count:${model.count}",
    )
  }

  private def install(): Unit =
    val react = js.Dynamic.global.React
    if react == null || js.isUndefined(react) then ()
    else Host.useReact(react)

  @JSExportTopLevel("Greeting")
  def greeting(props: js.Object): js.Any =
    install()
    asFunctionComponent(Greeting, d => d.label.asInstanceOf[String])(props)

  @JSExportTopLevel("CounterView")
  def counterView(props: js.Object): js.Any =
    install()
    Host.h(asConduitComponent(CounterView, _ => ()), props.asInstanceOf[js.Dictionary[js.Any]])
end Main
