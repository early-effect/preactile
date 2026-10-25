package preactile
import scala.scalajs.js

import zio.*
import zio.test.*

object ComponentsSpec extends PreactileSpec:
  val specs = suiteAll("Components"):
    test("A didCatch() error boundary should render an error condition"):
      object Catcher extends StatefulComponent[String, Option[js.Error]]:
        override def initialState(name: String): Option[js.Error] = None

        val SuperBorkedChildComponent: Component[String] = (name: String) =>
          parent.removeChild(parent) // this will throw an Exception
          E.span(name)

        override def render(name: String, error: Option[js.Error], instance: Catcher.Instance): VNode =
          error.fold(SuperBorkedChildComponent(name))(e => E.div(e.message))

        override def didCatch(e: js.Error, instance: Catcher.Instance): Unit = instance.setState(Some(e))
      end Catcher
      render(Catcher("foo"))
      checkAfter(10)(
        "<div>The node to be removed is not a child of this node.</div>"
      )
    test("Functional components should render") {
      val x = Component[String]: s =>
        E.div(
          E.div(s),
          E.div("bar"),
        )
      render(x("foo"))
      check("<div><div>foo</div><div>bar</div></div>")
    }
    test("A Component with a string prop and an instance selector and a class selector should render withRef") {
      object Simple extends Component[String] with InstanceDataSelector with ClassSelector:

        override lazy val classForClass: String = "simple"

        override def extractAttributeValue(instance: Simple.Instance): String = instance.props

        override def render(props: String): VNode = E.span(props)
      end Simple
      render(Simple("foo").withRef(e => e.setAttribute("foo", "bar")))
      check(
        "<span data-preactile-simple=\"foo\" class=\"simple\" foo=\"bar\">foo</span>"
      )
    }
  val spec = specs @@ TestAspect.sequential @@ TestAspect.withLiveClock
end ComponentsSpec
