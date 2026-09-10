package preactile.impl

import scala.scalajs.js

import preactile.Child
import preactile.Host
import preactile.impl.Preact.AnyDictionary

object Preactile:

  def h(`type`: js.Any, params: AnyDictionary | Null, children: js.Array[Child]): VNodeJS =
    Host.h(`type`, params, children)

  def h(`type`: js.Any, params: AnyDictionary | Null): VNodeJS = Host.h(`type`, params)

  def h(`type`: js.Any, params: AnyDictionary | Null, rawChildren: js.Any): VNodeJS =
    Host.h(`type`, params, rawChildren)
end Preactile
