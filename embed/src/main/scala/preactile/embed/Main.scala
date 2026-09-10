package preactile.embed

import scala.scalajs.js

import org.scalajs.dom

import preactile.*
import preactile.host.*

import conduit.*

/** Browser demo for host React 18 and host Preact. Host libraries are UMD globals; this bundle does not splice them.
  */
object Main:

  case class Counter(count: Int) derives Optics
  sealed trait CountAction extends Action
  object CountActions:
    case object Increment extends CountAction

  private val handler = handle[Counter, CountAction]:
    case CountActions.Increment => update(m => m.copy(count = m.count + 1))

  private val counterConduit = Conduit.make(Counter(0))(handler)

  object Greeting extends Component[String]:
    def render(props: String): VNode =
      E.div(A.id("greeting"), A.`class`("hi"), props)

  object ClickCounter extends StatefulComponent[Unit, Int]:
    override def initialState(props: Unit): Int = 0
    def render(props: Unit, state: Int, instance: Instance): VNode =
      E.button(
        A.id("stateful"),
        A.onClick(_ => instance.setState(state + 1)),
        s"state:$state",
      )

  val CounterView = counterConduit.component { model =>
    E.button(
      A.id("conduit"),
      A.onClick { _ =>
        counterConduit.unsafe(CountActions.Increment)
        counterConduit.unsafe.run(true)
      },
      s"count:${model.count}",
    )
  }

  def main(args: Array[String]): Unit =
    val host = Option(dom.document.body.getAttribute("data-host")).getOrElse("")
    host match
      case "react"  => mountReact()
      case "preact" => mountPreact()
      case other =>
        dom.window.console.error(s"Unknown data-host: $other")
  end main

  private def mountReact(): Unit =
    val react = js.Dynamic.global.React
    val domNs = js.Dynamic.global.ReactDOM
    Host.useReact(react)
    val greeting = asFunctionComponent(Greeting, d => d.label.asInstanceOf[String])
    val stateful = asStatefulComponent(ClickCounter, _ => ())
    val conduit  = asConduitComponent(CounterView, _ => ())
    val tree = react.createElement(
      "div",
      null,
      react.createElement("span", js.Dictionary("id" -> "host-chrome"), "host-react"),
      react.createElement(greeting, js.Dictionary("label" -> "from-preactile")),
      react.createElement(stateful, null),
      react.createElement(conduit, null),
    )
    val app = dom.document.getElementById("app")
    domNs.createRoot(app).render(tree)
  end mountReact

  private def mountPreact(): Unit =
    val preact = js.Dynamic.global.preact
    Host.usePreactHost(preact)
    val greeting = asFunctionComponent(Greeting, d => d.label.asInstanceOf[String])
    val stateful = asStatefulComponent(ClickCounter, _ => ())
    val conduit  = asConduitComponent(CounterView, _ => ())
    val h        = preact.h
    val tree = h(
      "div",
      null,
      h("span", js.Dictionary("id" -> "host-chrome"), "host-preact"),
      h(greeting, js.Dictionary("label" -> "from-preactile")),
      h(stateful, null),
      h(conduit, null),
    )
    preact.render(tree, dom.document.getElementById("app"))
  end mountPreact
end Main
