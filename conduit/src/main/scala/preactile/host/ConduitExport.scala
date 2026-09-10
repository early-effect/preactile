package preactile.host

import scala.scalajs.js

import preactile.ConduitComponent

def asConduitComponent[P, M <: Product, E, S](
    c: ConduitComponent[P, M, E, S],
    readProps: js.Dynamic => P,
): js.Dynamic =
  ConduitAdapters.exportClass(c, readProps)
