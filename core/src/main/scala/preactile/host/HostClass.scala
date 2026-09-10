package preactile.host

import scala.scalajs.js

import preactile.Host

object HostClass:

  def extend(base: js.Dynamic): js.Dynamic =
    if base == null || js.isUndefined(base) then
      throw IllegalStateException(
        "Host.use must be given componentClass (React.Component or preact.Component) for class adapters."
      )
    val ctor: js.ThisFunction1[js.Any, js.Dynamic, Unit] = (thiz, props) =>
      base.call(thiz, props)
      ()
    val ctorDyn = ctor.asInstanceOf[js.Dynamic]
    ctorDyn.prototype = js.Dynamic.global.Object.create(base.prototype)
    ctorDyn.prototype.constructor = ctorDyn
    js.Dynamic.global.Object.setPrototypeOf(ctorDyn, base)
    ctorDyn
  end extend

  def requireClass: js.Dynamic =
    Host.current.componentClass.getOrElse(
      throw IllegalStateException(
        "No host Component class installed. Pass componentClass to Host.use, or Host.useReact(React)."
      )
    )
end HostClass
