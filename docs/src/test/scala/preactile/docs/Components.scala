package preactile.docs

import specular.core.DocSpec
import specular.ziotest.DocSpecSuite

object Components extends DocSpecSuite:
  def doc = page("Components")(
    md"""
       # Components

       Preactile components are the building blocks of your UI. There are two main types:
       stateless (`PreactileComponent`) and stateful (`StatefulComponent`).

       ## Stateless components

       Use `PreactileComponent` when your output depends only on its props:

       ```scala mdoc:compile-only
       import preactile.dsl._

       object Greeting extends PreactileComponent:
         type Props = String
         def render(props: Props) = h1(s"Hello, $props!")
       ```

       Compose them like any other element:

       ```scala mdoc:compile-only
       import preactile.dsl._

       object App extends PreactileComponent:
         type Props = Unit
         def render(props: Props) = div(
           Greeting("Alice"),
           Greeting("Bob"),
         )
       ```

       ## Components with typed props

       Define a case class for complex prop types:

       ```scala mdoc:compile-only
       import preactile.dsl._

       case class ButtonProps(label: String, onClick: () => Unit)

       object Button extends PreactileComponent:
         type Props = ButtonProps
         def render(props: Props) = button(
           `class` := "btn",
           onclick := (_ => props.onClick()),
           props.label,
         )
       ```

       See the [Examples](/Examples) page for a live interactive demo of component composition.
       """,
  )
