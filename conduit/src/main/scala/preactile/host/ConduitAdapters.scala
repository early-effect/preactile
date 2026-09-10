package preactile.host

import scala.scalajs.js

import preactile.*
import preactile.dictionaryNames.*

import conduit.*

object ConduitAdapters:

  def hostType[P, M <: Product, Ev, S](c: ConduitComponent[P, M, Ev, S]): js.Dynamic =
    conduitClass(c, Host.scalaProps[P])

  def exportClass[P, M <: Product, Ev, S](
      c: ConduitComponent[P, M, Ev, S],
      readProps: js.Dynamic => P,
  ): js.Dynamic =
    conduitClass(c, raw => readExportProps(raw, readProps))

  private def readExportProps[P](raw: js.Dynamic, readProps: js.Dynamic => P): P =
    if raw == null || js.isUndefined(raw) then readProps(raw)
    else
      val inner = raw.selectDynamic(PropsFieldName)
      if inner != null && !js.isUndefined(inner) then inner.asInstanceOf[P]
      else readProps(raw)

  private def conduitClass[P, M <: Product, Ev, S](
      c: ConduitComponent[P, M, Ev, S],
      read: js.Dynamic => P,
  ): js.Dynamic =
    val base = HostClass.requireClass
    val ctor: js.ThisFunction1[js.Any, js.Dynamic, Unit] = (thiz, rawProps) =>
      base.call(thiz, rawProps)
      val d = thiz.asInstanceOf[js.Dynamic]
      d.state =
        js.Dictionary(StateFieldName -> c.conduit.unsafe.zoom(c.lens).asInstanceOf[js.Any]).asInstanceOf[js.Dynamic]
      c.willMount(HostInstance[P, S](d, read, Host.scalaState[S]))
      ()
    val ctorDyn = ctor.asInstanceOf[js.Dynamic]
    ctorDyn.prototype = js.Dynamic.global.Object.create(base.prototype)
    ctorDyn.prototype.constructor = ctorDyn
    js.Dynamic.global.Object.setPrototypeOf(ctorDyn, base)
    val proto = ctorDyn.prototype
    val inst  = (thiz: js.Dynamic) => HostInstance[P, S](thiz, read, Host.scalaState[S])

    val renderFn: js.ThisFunction2[js.Dynamic, js.Dynamic, js.Dynamic, js.Any] = (thiz, p, s) =>
      val rawP  = if p == null || js.isUndefined(p) then thiz.props else p
      val rawS  = if s == null || js.isUndefined(s) then thiz.state else s
      val i     = inst(thiz)
      val props = read(rawP)
      val state = Host.scalaState[S](rawS)
      c.addSelectors(c.render(props, state), i).value
    proto.render = renderFn

    val should: js.ThisFunction3[js.Dynamic, js.Dynamic, js.Dynamic, js.Dynamic, Boolean] =
      (thiz, nextProps, nextState, _) => c.shouldUpdate(read(nextProps), Host.scalaState[S](nextState), inst(thiz))
    proto.shouldComponentUpdate = should

    val didMount: js.ThisFunction0[js.Dynamic, Unit] = thiz =>
      val i                         = inst(thiz)
      val unsub: Listener[M, Ev, S] = c.conduit.unsafe.subscribe(c.lens)(x => i.setState(x))
      thiz.updateDynamic("__preactile_unsub")(unsub.asInstanceOf[js.Any])
      c.didMount(i)
    proto.componentDidMount = didMount

    val willUn: js.ThisFunction0[js.Dynamic, Unit] = thiz =>
      val unsub = thiz.selectDynamic("__preactile_unsub")
      if unsub != null && !js.isUndefined(unsub) then
        c.conduit.unsafe.unsubscribe(unsub.asInstanceOf[Listener[M, Ev, S]])
      c.willUnMount(inst(thiz))
    proto.componentWillUnmount = willUn

    val didUpdate: js.ThisFunction3[js.Dynamic, js.Dynamic, js.Dynamic, js.Dynamic, Unit] =
      (thiz, oldProps, oldState, snapshot) =>
        c.didUpdate(
          read(oldProps),
          Host.scalaState[S](oldState),
          inst(thiz),
          snapshot.asInstanceOf[js.UndefOr[c.Instance]],
        )
    proto.componentDidUpdate = didUpdate

    val didCatch: js.ThisFunction1[js.Dynamic, js.Error, Unit] = (thiz, e) => c.didCatch(e, inst(thiz))
    proto.componentDidCatch = didCatch
    ctorDyn.displayName = c.classForClass
    ctorDyn
  end conduitClass
end ConduitAdapters
