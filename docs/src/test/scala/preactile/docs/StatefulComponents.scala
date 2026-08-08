package preactile.docs

import specular.core.DocSpec
import specular.ziotest.DocSpecSuite

object StatefulComponents extends DocSpecSuite:
  def doc = page("Stateful Components")(
    md"""
       # Stateful Components

       Use `StatefulComponent` when your component needs to manage internal state.

       ## Basic counter

       ```scala mdoc:compile-only
       import preactile.dsl._

       object Counter extends StatefulComponent[Int]:
         type Props = Unit
         def initialState(props: Props) = 0

         def render(state: Int, props: Props) = div(
           span(s"Count: $state"),
           button(onclick := (_ => setState(_ + 1)), "+"),
           button(onclick := (_ => setState(math.max(0, _ - 1))), "-"),
         )
       ```

       ## State with props

       Combine initial props with mutable state:

       ```scala mdoc:compile-only
       import preactile.dsl._

       case class TimerProps(intervalMs: Int)

       object Timer extends StatefulComponent[Int]:
         type Props = TimerProps
         def initialState(props: Props) = 0

         def render(state: Int, props: Props) = div(
           span(s"Elapsed: ${state}ms"),
           button(onclick := (_ => setState(0)), "Reset"),
         )
       ```

       See the [Examples](/Examples) page for a live interactive counter demo.
       """,
  )
