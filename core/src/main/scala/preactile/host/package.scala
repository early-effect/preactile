package preactile.host

import scala.scalajs.js

import preactile.Child
import preactile.Component
import preactile.Host
import preactile.HostChild
import preactile.StatefulComponent

def hostChild(raw: js.Any): Child = HostChild(raw)

def asFunctionComponent[P](c: Component[P], readProps: js.Dynamic => P): js.Function1[js.Object, js.Any] =
  Adapters.exportFunction(c, readProps)

/** Subclass of the installed host `Component` (`React.Component` / `preact.Component`). */
def asClassComponent[P](c: Component[P], readProps: js.Dynamic => P): js.Dynamic =
  Adapters.exportClass(c, readProps)

def asStatefulComponent[P, S](c: StatefulComponent[P, S], readProps: js.Dynamic => P): js.Dynamic =
  Adapters.exportStatefulClass(c, readProps)

def asComponent[P](c: Component[P], readProps: js.Dynamic => P): js.Any =
  Host.current.componentClass.fold[js.Any](asFunctionComponent(c, readProps))(_ => asClassComponent(c, readProps))
