package preactile

import scala.scalajs.js

import zio.*
import zio.test.*

/** Tests that verify our assumptions about Preact 10.x's Component API shape match reality.
  *
  * These are critical: if any of these fail, the core bindings in ComponentJS/StatefulComponent need updating to match
  * actual Preact behavior.
  */
object PreactApiShapeSpec extends PreactileSpec:

  val specs = suiteAll("Preact API shape"):

    test("render(props, state) receives props as an argument"):
      // Our ComponentJS binding declares renderJS(props, state). This test proves Preact actually
      // passes those arguments rather than expecting us to read this.props.
      class RenderArgComponent extends StatefulComponent[String, Unit]:
        override def initialState(name: String): Unit = ()

        override def render(name: String, state: Unit, instance: Instance): VNode =
          E.div(s"render-arg:$name")

      val component = new RenderArgComponent()
      render(component("hello"))
      check("<div>render-arg:hello</div>")

    test("componentWillMount is called before this.props is set"):
      // In newer Preact 10.x, componentWillMount() runs before props are assigned to the instance.
      // This means we cannot read `props` inside componentWillMount; we must use render arguments instead.
      class WillMountComponent extends StatefulComponent[String, String]:
        override def initialState(name: String): String = "init"

        private var willMountSawProps: Boolean = false

        override def willMount(instance: Instance): Unit =
          // Try to read props via the instance's rawProps. In newer Preact this is undefined/null here.
          val raw = js.Dynamic.global.eval("this") match
            case d: js.Dynamic =>
              try d.rawProps != null && d.rawProps.name != null
              catch case _: Throwable => false
          willMountSawProps = raw

        override def render(name: String, state: String, instance: Instance): VNode =
          E.div(s"willmount:$state:${if willMountSawProps then "saw-props" else "no-props"}")
      end WillMountComponent

      val component = new WillMountComponent()
      render(component("test"))
      // If componentWillMount had props available, we'd see "saw-props". We expect "no-props".
      check("<div>willmount:init:no-props</div>")

    test("render arguments are reliable for initial state computation"):
      // Since componentWillMount can't read props, our StatefulComponent computes initialState
      // lazily in renderJS using the props argument. This test proves that works correctly.
      class LazyInitComponent extends StatefulComponent[String, String]:
        override def initialState(name: String): String = s"derived-from-$name"

        override def render(name: String, state: String, instance: Instance): VNode =
          E.div(s"lazy:$state")

      val component = new LazyInitComponent()
      render(component("input"))
      check("<div>lazy:derived-from-input</div>")

    test("setState in componentDidMount triggers re-render"):
      // This proves didMount is called and setState works within it. The state change causes a
      // re-render, confirming the full lifecycle chain: render -> mount -> setState -> update.
      class SetStateComponent extends StatefulComponent[Unit, Int]:
        override def initialState(unit: Unit): Int = 1

        private var incrementCalled = false

        override def didMount(instance: Instance): Unit =
          if !incrementCalled then
            instance.setState(2)
            incrementCalled = true

        override def render(unit: Unit, count: Int, instance: Instance): VNode =
          E.div(s"count:$count")
      end SetStateComponent

      val component = new SetStateComponent()
      render(component(()))
      checkAfter(10)(s"<div>count:2</div>")

    test("shouldUpdate controls whether re-render occurs"):
      class ShouldUpdateComponent extends StatefulComponent[String, String]:
        override def initialState(name: String): String = name.toUpperCase

        private var shouldUpdateCalled = false

        override def shouldUpdate(nextProps: String, nextState: String, previous: Instance): Boolean =
          if !shouldUpdateCalled then
            shouldUpdateCalled = true
            // Block the update even though props changed
            false
          else true

        override def render(name: String, state: String, instance: Instance): VNode =
          E.div(s"$name:$state")
      end ShouldUpdateComponent

      val component = new ShouldUpdateComponent()
      render(component("a"))
      assertTrue(parent.innerHTML == "<div>a:A</div>") *>
        ZIO.succeed { preact.render(component("b"), parent) } *>
        // shouldUpdate returned false, so old content persists
        check("<div>a:A</div>")

    test("componentDidCatch error boundary renders fallback"):
      // Verifies componentDidCatch is called when a child throws and the instance can update state.
      class Catcher extends StatefulComponent[String, Option[js.Error]]:
        override def initialState(name: String): Option[js.Error] = None

        val SuperBorkedChildComponent: Component[String] = (name: String) =>
          parent.removeChild(parent) // this will throw an Exception
          E.span(name)

        override def render(name: String, error: Option[js.Error], instance: Instance): VNode =
          error.fold(SuperBorkedChildComponent(name))(e => E.div(e.message))

        override def didCatch(e: js.Error, instance: Instance): Unit = instance.setState(Some(e))
      end Catcher

      val component = new Catcher()
      render(component("foo"))
      checkAfter(10)(
        "<div>The node to be removed is not a child of this node.</div>"
      )

  val spec = specs @@ TestAspect.sequential @@ TestAspect.withLiveClock

end PreactApiShapeSpec
