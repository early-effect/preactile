package preactile.docs

import specular.client.Mounter

object ComponentsDemo:

  val mounter: Mounter = Mounter.sync(el => preactile.preact.render(App(()), el))

  // specular:begin demo
  import preactile.*

  case class ButtonProps(label: String, onClick: () => Unit)

  object Button extends Component[ButtonProps]:
    def render(props: ButtonProps): VNode = E.button(
      A.`class`("demo-button"),
      A.onClick(_ => props.onClick()),
      props.label,
    )

  object App extends StatefulComponent[Unit, Int]:
    override def initialState(props: Unit): Int = 0

    def render(props: Unit, state: Int, instance: Instance): VNode = E.div(
      A.`class`("components-demo"),
      E.p(s"Button clicked $state times."),
      Button(ButtonProps("Click me!", () => instance.setState(state + 1))),
    )
  // specular:end
end ComponentsDemo
