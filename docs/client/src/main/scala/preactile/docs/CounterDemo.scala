package preactile.docs

import specular.client.Mounter

object CounterDemo:

  val mounter: Mounter = Mounter.sync(el => preactile.preact.render(Counter(()), el))

  // specular:begin demo
  import preactile.*

  object Counter extends StatefulComponent[Unit, Int]:
    override def initialState(props: Unit): Int = 0

    def render(props: Unit, state: Int, instance: Instance): VNode =
      E.div(
        A.`class`("counter-demo"),
        E.button(A.onClick(_ => instance.setState(state - 1)), "-"),
        E.span(" ", E.strong(state.toString), " "),
        E.button(A.onClick(_ => instance.setState(state + 1)), "+"),
      )
  end Counter
  // specular:end
end CounterDemo
