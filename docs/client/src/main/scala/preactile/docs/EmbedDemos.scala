package preactile.docs

import scala.scalajs.js

import org.scalajs.dom

import specular.client.Mounter

import preactile.*
import preactile.host.*
import preactile.impl.Preact as PreactNs
import preactile.impl.VNodeJS

object EmbedSimple:

  // specular:begin simple
  import preactile.*

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
  // specular:end
end EmbedSimple

object EmbedTodo:

  // specular:begin todo
  import preactile.host.*

  val HostApp = asConduitComponent(TodoDemo.App, _ => ())
  // specular:end
end EmbedTodo

object EmbedReactDemo:

  // Look up host UMD on `window`, not `js.Dynamic.global.React`. The latter is a free
  // identifier and spliceFull/Closure rejects it as undeclared.
  private def browserGlobal(name: String): js.Dynamic =
    dom.window.asInstanceOf[js.Dynamic].selectDynamic(name)

  private def reactNs: js.Dynamic  = browserGlobal("React")
  private def reactDom: js.Dynamic = browserGlobal("ReactDOM")

  private def ensureReact(): Unit =
    if !Host.isInstalled then Host.useReact(reactNs)

  val simpleMounter: Mounter = Mounter.sync { el =>
    ensureReact()
    val greeting = asFunctionComponent(EmbedSimple.Greeting, d => d.label.asInstanceOf[String])
    val stateful = asStatefulComponent(EmbedSimple.ClickCounter, _ => ())
    val tree = reactNs.createElement(
      "div",
      null,
      reactNs.createElement("span", js.Dictionary("id" -> "host-chrome"), "host-react"),
      reactNs.createElement(greeting, js.Dictionary("label" -> "from-preactile")),
      reactNs.createElement(stateful, null),
    )
    reactDom.createRoot(el).render(tree)
  }

  val todoMounter: Mounter = Mounter.sync { el =>
    ensureReact()
    reactDom.createRoot(el).render(reactNs.createElement(EmbedTodo.HostApp, null))
  }
end EmbedReactDemo

object EmbedPreactDemo:

  private def preactNs: js.Dynamic = PreactNs.asInstanceOf[js.Dynamic]

  private def ensurePreact(): Unit =
    if !Host.isInstalled then Host.usePreactHost(preactNs)

  val simpleMounter: Mounter = Mounter.sync { el =>
    ensurePreact()
    val greeting = asFunctionComponent(EmbedSimple.Greeting, d => d.label.asInstanceOf[String])
    val stateful = asStatefulComponent(EmbedSimple.ClickCounter, _ => ())
    val h        = preactNs.h
    val tree = h(
      "div",
      null,
      h("span", js.Dictionary("id" -> "host-chrome"), "host-preact"),
      h(greeting, js.Dictionary("label" -> "from-preactile")),
      h(stateful, null),
    )
    PreactNs.render(tree.asInstanceOf[VNodeJS], el)
  }

  val todoMounter: Mounter = Mounter.sync { el =>
    ensurePreact()
    PreactNs.render(
      preactNs.h(EmbedTodo.HostApp, null).asInstanceOf[VNodeJS],
      el,
    )
  }
end EmbedPreactDemo
