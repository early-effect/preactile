package preactile.docs

import specular.client.Mounter

object ElementDslDemo:

  val mounter: Mounter = Mounter.sync(el => preactile.preact.render(App(()), el))

  // specular:begin demo
  import preactile.*

  object App extends StatefulComponent[Unit, Boolean]:
    override def initialState(props: Unit): Boolean = false

    def render(props: Unit, state: Boolean, instance: Instance): VNode = E.div(
      A.`class`("element-dsl-demo"),
      E.h4("Element DSL demo"),
      E.input(
        A.`type`("checkbox"),
        A.checked(state),
        A.onChange(_ => instance.setState(!state)),
      ),
      E.span(" Enable feature"),
      when(state)(
        E.div(
          A.`class`("feature-enabled"),
          E.p("Feature is now enabled!"),
          E.a(A.href("#"), A.onClick(_ => ()), "Learn more"),
        )
      ),
    )
  end App
  // specular:end
end ElementDslDemo
