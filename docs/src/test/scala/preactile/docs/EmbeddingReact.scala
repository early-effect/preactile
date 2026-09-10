package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object EmbeddingReact extends DocSpecSuite:
  def doc = page("Embedding in React")(
    md"""
# Embedding in React 18

This page is a host React 18 tree. Preactile components are exported with
`asFunctionComponent` / `asStatefulComponent` / `asConduitComponent` and mounted
with `React.createElement` into the SSR placeholders below. The host owns
`createElement`. Do not call `preactile.preact.render` on this page.

See [Embedding in Preact](/EmbeddingInPreact) for the same demos under host Preact.

## Install the host once

```scala
import scala.scalajs.js
import preactile.Host

Host.useReact(js.Dynamic.global.React)
```

`class` becomes `className` (and `for` becomes `htmlFor`) only for a React host.

## Simple: greeting and a stateful counter

Host chrome (`span#host-chrome`) sits next to Preactile children.
""",
    exampleDom("embed-react-simple").fromSource(
      "docs/client/src/main/scala/preactile/docs/EmbedDemos.scala",
      "simple",
    ),
    md"""
## Complex: conduit todo

The same todo UI as [Conduit Integration](/ConduitIntegration), exported with
`asConduitComponent` and rendered by React. Actions are still `unsafe` + `run`.
""",
    exampleDom("embed-react-todo").fromSource(
      "docs/client/src/main/scala/preactile/docs/EmbedDemos.scala",
      "todo",
    ),
    md"""
## What not to do

- Do not pass a Preactile `Component.apply` VNode to React. It has no
  `$$typeof: Symbol.for('react.element')`.
- Do not call `preactile.preact.render` after `Host.useReact`.
- Do not splice a second Preact into a bundle that already has React.
""",
  )
end EmbeddingReact
