package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Components extends DocSpecSuite:
  def doc = page("Components")(
    md"""
# Components

Preactile components are the building blocks of your UI. There are two main types:
stateless (`Component`) and stateful (`StatefulComponent`).

## Stateless components

Use `Component` when your output depends only on its props:

```scala
import preactile.*

object Greeting extends Component[String]:
  def render(props: String): VNode = E.h1(s"Hello, $$props!")
```

Compose them like any other element:

```scala
import preactile.*

object App extends Component[Unit]:
  def render(props: Unit): VNode = E.div(
    Greeting("Alice"),
    Greeting("Bob"),
  )
```

## Components with typed props

Define a case class for complex prop types:

```scala
import preactile.*

case class ButtonProps(label: String, onClick: () => Unit)

object Button extends Component[ButtonProps]:
  def render(props: ButtonProps): VNode = E.button(
    A.`class`("btn"),
    A.onClick(_ => props.onClick()),
    props.label,
  )
```

## Live demo: component composition

This shows a typed `Button` component composed inside an `App` that manages click count state:
""",
    exampleDom("components-demo").fromSource(
      "docs/client/src/main/scala/preactile/docs/ComponentsDemo.scala",
      "demo",
    ),
    md"""
The `ButtonProps` case class gives you full type safety for props, and the parent component
passes a callback that updates its own state.

See [Stateful Components](/StatefulComponents) for `setState`, and [Examples](/Examples)
for the other live demos.
""",
  )
end Components
