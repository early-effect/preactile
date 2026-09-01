package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Examples extends DocSpecSuite:
  def doc = page("Examples")(
    md"""
# Examples

Live interactive demos live on the page that teaches them. Each one is a real Preactile
component mounted into the docs site:

- [Overview](/Overview): a stateless greeting (`Component[Unit]`).
- [Components](/Components): typed props and composition (`Button` inside a click counter).
- [Stateful Components](/StatefulComponents): `initialState` and `setState` on a counter.
- [Conduit Integration](/ConduitIntegration): a todo app with `Conduit.make`, lensed
  subscriptions, and `conduit.unsafe(action)`.
- [Element DSL](/ElementDsl): attributes, events, and `when` for conditional content.
- [Styling](/Styling): `CssClass` plus inline `A.style` declarations.

Setup and bundling (sbt-splice, mount code) is on [Mounting](/Mounting).
"""
  )
end Examples
