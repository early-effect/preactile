package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Embedding extends DocSpecSuite:
  def doc = page("Embedding")(
    md"""
# Embedding in React (and host Preact)

Preactile can render through a host React 18 (or host Preact) tree. The host owns
`createElement`. You do not call `preact.render`, and the embed bundle must not
splice a second Preact.

Owned apps are unchanged: `preactile.preact.render` still installs spliced Preact.

## Install the host once

At module init, before any `E.div` / `Component.apply`:

```scala
import scala.scalajs.js
import preactile.Host

val react = js.Dynamic.global.React // or the module you were given
Host.useReact(react)
```

`Host.useReact` takes `createElement`, `Fragment`, `Component`, and hooks from that
namespace. A host Preact app can call `Host.usePreactHost(preactNs, hooksNs)` instead.

`class` becomes `className` (and `for` becomes `htmlFor`) only for a React host.

## Export a component

```scala
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import preactile.*
import preactile.host.*

object ConsentCard extends Component[Grants]:
  def render(grants: Grants): VNode =
    E.div(A.`class`("card"), E.p(grants.summary))

@JSExportTopLevel("ConsentCard")
val ConsentCardHost = asClassComponent(ConsentCard, d => d.grants.asInstanceOf[Grants])
```

`asClassComponent` / `asStatefulComponent` subclass the host `Component`
(`Host.Component`). That is the path that keeps `setState`, lifecycle, and conduit
subscriptions. `asFunctionComponent` is the thinner export when you only need
`render`.

The host then registers the export with its slot API, or uses it as `<ConsentCard grants={...} />`.
Do not wrap it in `useEffect` + `preact.render`.

## Stateful and conduit

`asStatefulComponent` maps `initialState`, `deriveState`, `shouldUpdate`, `didMount`,
`didUpdate`, `willUnMount`, and `didCatch` onto the host class.

Conduit stays a first-class Preactile component. Depend on `preactile-conduit` and
export with `asConduitComponent`:

```scala
val CounterView = counterConduit.component { model =>
  E.div(
    E.strong(model.count.toString),
    E.button(
      A.onClick { _ =>
        counterConduit.unsafe(CountActions.Increment)
        counterConduit.unsafe.run(true)
      },
      "+",
    ),
  )
}

@JSExportTopLevel("CounterView")
val CounterViewHost = asConduitComponent(CounterView, _ => ())
```

Actions are still `unsafe` + `run`. The host is only the renderer. Unmount
unsubscribes.

## Host children

Pass an already-constructed React/Preact element through with `hostChild(el)` so
official icons or slot chrome are not wrapped or double-mounted.

## What not to do

- Do not pass a Preactile `Component.apply` VNode to React. It has no
  `$$typeof: Symbol.for('react.element')`. Always export through `as*Component`.
- Do not call `preactile.preact.render` in the same bundle after `Host.useReact`.
- Do not add `spliceLibs` for Preact on an embed project that already has React.

See [Mounting](/Mounting) for owned Preact apps, and [Conduit Integration](/ConduitIntegration)
for the conduit DSL.
"""
  )
end Embedding
