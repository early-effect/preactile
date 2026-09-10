package preactile.host

import scala.scalajs.js

import org.scalajs.dom

import zio.*
import zio.test.*

import preactile.*

object HostSpec extends PreactileSpec:

  private def reactElementSymbol: js.Symbol =
    js.Symbol.forKey("react.element")

  private def installReact(): Unit =
    js.Dynamic.global.globalThis.updateDynamic("IS_REACT_ACT_ENVIRONMENT")(true)
    Host.reset()
    Host.useReact(ReactNs.asInstanceOf[js.Dynamic])

  private def withReact[R](f: => R): R =
    installReact()
    try f
    finally Host.reset()

  private def act(thunk: => Unit): Unit =
    ReactAct(() =>
      thunk; ().asInstanceOf[js.Any]
    )
    ()

  private def mountReact(element: js.Any): (dom.Element, ReactRoot) =
    val el   = dom.document.createElement("div")
    val root = ReactDomClient.createRoot(el)
    act(root.render(element))
    (el, root)

  val specs = suiteAll("Host"):

    test("building a VNode without a renderer throws"):
      val prev = Host.isInstalled
      Host.reset()
      val err =
        try
          E.div("x")
          None
        catch case e: IllegalStateException => Some(e.getMessage)
      if prev then preact.rerender()
      assertTrue(err.exists(_.contains("No renderer installed")))

    test("a Preactile VNode is not a React element"):
      render(E.div("preact-path"))
      val raw = E.span("raw").vNode.asInstanceOf[js.Dynamic]
      val t   = raw.selectDynamic("$$typeof")
      assertTrue(js.isUndefined(t) || t == null)

    test("asFunctionComponent produce a React element with $$typeof"):
      withReact {
        object Hello extends Component[String]:
          def render(props: String): VNode = E.span(props)
        val fc  = asFunctionComponent(Hello, d => d.label.asInstanceOf[String])
        val el  = ReactNs.createElement(fc, js.Dictionary("label" -> "hi"))
        val tpe = el.selectDynamic("$$typeof")
        assertTrue(tpe == reactElementSymbol)
      }

    test("React host maps class to className"):
      withReact {
        object Btn extends Component[Unit]:
          def render(props: Unit): VNode = E.button(A.`class`("go"), "Go")
        val fc         = asFunctionComponent(Btn, _ => ())
        val (el, root) = mountReact(ReactNs.createElement(fc, js.Dictionary[js.Any]()))
        val html       = el.innerHTML
        act(root.unmount())
        assertTrue(html.contains("class=\"go\"") && html.contains("Go"))
      }

    test("stateless adapter mounts, updates, and unmounts"):
      withReact {
        var unmounted = false
        object Hello extends Component[String]:
          def render(props: String): VNode                   = E.div(s"hello:$props")
          override def willUnMount(instance: Instance): Unit = unmounted = true
        val fc         = asClassComponent(Hello, d => d.label.asInstanceOf[String])
        val (el, root) = mountReact(ReactNs.createElement(fc, js.Dictionary("label" -> "a")))
        val first      = el.innerHTML
        act(root.render(ReactNs.createElement(fc, js.Dictionary("label" -> "b"))))
        val second = el.innerHTML
        act(root.unmount())
        assertTrue(first.contains("hello:a") && second.contains("hello:b") && unmounted)
      }

    test("stateful adapter setState from click and didMount"):
      withReact {
        object Counter extends StatefulComponent[Unit, Int]:
          override def initialState(props: Unit): Int = 1
          override def didMount(instance: Instance): Unit =
            if instance.state == 1 then instance.setState(2)
          def render(props: Unit, state: Int, instance: Instance): VNode =
            E.div(
              E.span(s"count:$state"),
              E.button(
                A.id("inc"),
                A.onClick(_ => instance.setState(state + 1)),
                "+",
              ),
            )
        end Counter
        val cls        = asStatefulComponent(Counter, _ => ())
        val (el, root) = mountReact(ReactNs.createElement(cls, null))
        val html       = el.innerHTML
        act:
          Option(el.querySelector("#inc")).foreach(_.asInstanceOf[dom.html.Button].click())
        val html2 = el.innerHTML
        act(root.unmount())
        assertTrue(html.contains("count:2") && html2.contains("count:3"))
      }

    test("shouldUpdate false keeps old DOM"):
      withReact {
        object Frozen extends StatefulComponent[String, String]:
          override def initialState(name: String): String = name
          override def shouldUpdate(nextProps: String, nextState: String, previous: Instance): Boolean =
            false
          def render(props: String, state: String, instance: Instance): VNode =
            E.div(s"$props:$state")
        val cls        = asStatefulComponent(Frozen, d => d.label.asInstanceOf[String])
        val (el, root) = mountReact(ReactNs.createElement(cls, js.Dictionary("label" -> "a")))
        val first      = el.innerHTML
        act(root.render(ReactNs.createElement(cls, js.Dictionary("label" -> "b"))))
        val second = el.innerHTML
        act(root.unmount())
        assertTrue(first.contains("a:a") && second.contains("a:a"))
      }

    test("hostChild passes a React element through"):
      withReact {
        object Wrap extends Component[Unit]:
          def render(props: Unit): VNode =
            val icon = ReactNs.createElement("em", js.Dictionary("id" -> "icon"), "x")
            E.div(hostChild(icon), " after")
        val fc         = asFunctionComponent(Wrap, _ => ())
        val (el, root) = mountReact(ReactNs.createElement(fc, null))
        val html       = el.innerHTML
        act(root.unmount())
        assertTrue(html.contains("id=\"icon\"") && html.contains("after"))
      }

    test("nested Preactile components render under React"):
      withReact {
        object Inner extends Component[String]:
          def render(props: String): VNode = E.strong(props)
        object Outer extends Component[String]:
          def render(props: String): VNode = E.div(Inner(props))
        val fc         = asClassComponent(Outer, d => d.label.asInstanceOf[String])
        val (el, root) = mountReact(ReactNs.createElement(fc, js.Dictionary("label" -> "nest")))
        val html       = el.innerHTML
        act(root.unmount())
        assertTrue(html.contains("<strong>nest</strong>"))
      }

  val spec = specs @@ TestAspect.sequential
end HostSpec
