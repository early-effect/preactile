package preactile.host

import scala.scalajs.js

import org.scalajs.dom

import zio.*
import zio.test.*

import conduit.*
import preactile.*

object ConduitHostSpec extends ZIOSpecDefault:

  case class Counter(count: Int) derives Optics

  sealed trait CountAction extends Action
  object CountActions:
    case object Increment extends CountAction

  private def act(thunk: => Unit): Unit =
    ReactAct(() =>
      thunk; ().asInstanceOf[js.Any]
    )
    ()

  val specs = suiteAll("Conduit host"):

    test("conduit adapter zooms, updates on action, and unsubscribes"):
      js.Dynamic.global.globalThis.updateDynamic("IS_REACT_ACT_ENVIRONMENT")(true)
      Host.reset()
      Host.useReact(ReactNs.asInstanceOf[js.Dynamic])
      val handler = handle[Counter, CountAction]:
        case CountActions.Increment => update(m => m.copy(count = m.count + 1))
      val cduit = Conduit.make(Counter(0))(handler)
      val view = cduit.component { model =>
        E.div(s"count:${model.count}")
      }
      val cls  = asConduitComponent(view, _ => ())
      val el   = dom.document.createElement("div")
      val root = ReactDomClient.createRoot(el)
      act(root.render(ReactNs.createElement(cls, null)))
      val first = el.innerHTML
      act:
        cduit.unsafe(CountActions.Increment)
        cduit.unsafe.run(true)
      val second = el.innerHTML
      act(root.unmount())
      act:
        cduit.unsafe(CountActions.Increment)
        cduit.unsafe.run(true)
      val third = el.innerHTML
      Host.reset()
      assertTrue(first.contains("count:0") && second.contains("count:1") && !third.contains("count:2"))

  val spec = specs @@ TestAspect.sequential
end ConduitHostSpec
