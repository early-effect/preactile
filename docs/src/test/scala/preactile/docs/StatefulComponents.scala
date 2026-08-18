package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object StatefulComponents extends DocSpecSuite:
  def doc = page("Stateful Components")(
    md"""
       # Stateful Components

       Use `StatefulComponent` when your component needs to manage internal state.
       The component initializes state from props, then re-renders whenever state changes.

       ## Live counter

       This is a real Preactile component rendered live in your browser. Click the buttons:

    """,
    exampleDom("stateful-counter").fromSource(
      "docs/client/src/main/scala/preactile/docs/CounterDemo.scala",
      "demo",
    ),
    md"""
       Key points:

       - `initialState` derives the starting state from props (called once on mount)
       - `render` receives both current props and current state as arguments
       - `instance.setState(newState)` triggers a re-render with the updated state

       ## State derived from props

       Initial state can depend on incoming props:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       case class TimerProps(initialDelayMs: Int)

       object Timer extends StatefulComponent[TimerProps, Int]:
         override def initialState(props: TimerProps): Int = props.initialDelayMs

         def render(props: TimerProps, state: Int, instance: Instance): VNode =
           div(
             span(s"Elapsed: $${state}ms"),
             button(A.onClick(_ => instance.setState(0)), "Reset"),
           )
       ```

       ## Lifecycle hooks

       StatefulComponent provides lifecycle hooks for side effects:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       object DataFetcher extends StatefulComponent[Unit, String]:
         override def initialState(props: Unit): String = "Loading..."

         override def didMount(instance: Instance): Unit =
           // Fetch data after mount; update state when ready
           instance.setState("Data loaded!")

         override def willUnMount(instance: Instance): Unit =
           // Clean up subscriptions or timers here

         def render(props: Unit, state: String, instance: Instance): VNode =
           p(state)
       ```

       See the [Examples](/Examples) page for more interactive demos.
       """,
  )
end StatefulComponents
