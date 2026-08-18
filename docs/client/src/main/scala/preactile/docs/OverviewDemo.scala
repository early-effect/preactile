package preactile.docs

import specular.client.Mounter

object OverviewDemo:

  val mounter: Mounter = Mounter.sync(el => preactile.preact.render(Greeting(()), el))

  // specular:begin demo
  import preactile.*

  object Greeting extends Component[Unit]:
    def render(props: Unit): VNode = E.div(
      A.`class`("overview-greeting"),
      E.h3("Hello from Preactile!"),
      E.p("This is a live component rendered in your browser."),
    )
  // specular:end
end OverviewDemo
