package preactile.host

import scala.scalajs.js

import preactile.*
import preactile.dictionaryNames.*

object Adapters:

  def statelessType[P](c: Component[P]): js.Dynamic =
    Host.current.componentClass.fold(statelessFunction(c))(_ => statelessClass(c, Host.scalaProps[P]))

  def statefulType[P, S](c: StatefulComponent[P, S]): js.Dynamic =
    statefulClass(c, Host.scalaProps[P])

  def exportFunction[P](c: Component[P], readProps: js.Dynamic => P): js.Function1[js.Object, js.Any] =
    (raw: js.Object) =>
      val props = readExportProps(raw.asInstanceOf[js.Dynamic], readProps)
      c.addSelectors(c.render(props), DummyInstance(props)).value

  def exportClass[P](c: Component[P], readProps: js.Dynamic => P): js.Dynamic =
    statelessClass(c, raw => readExportProps(raw, readProps))

  def exportStatefulClass[P, S](c: StatefulComponent[P, S], readProps: js.Dynamic => P): js.Dynamic =
    statefulClass(c, raw => readExportProps(raw, readProps))

  private def readExportProps[P](raw: js.Dynamic, readProps: js.Dynamic => P): P =
    if raw == null || js.isUndefined(raw) then readProps(raw)
    else
      val inner = raw.selectDynamic(PropsFieldName)
      if inner != null && !js.isUndefined(inner) then inner.asInstanceOf[P]
      else readProps(raw)

  private def statelessFunction[P](c: Component[P]): js.Dynamic =
    val f: js.Function1[js.Dynamic, js.Any] = raw =>
      val props = Host.scalaProps[P](raw)
      c.addSelectors(c.render(props), DummyInstance(props)).value
    f.asInstanceOf[js.Dynamic]

  private def statelessClass[P](c: Component[P], read: js.Dynamic => P): js.Dynamic =
    val ctor  = HostClass.extend(HostClass.requireClass)
    val proto = ctor.prototype
    val inst  = (thiz: js.Dynamic) => HostInstance[P, Nothing](thiz, read, Host.scalaState[Nothing])
    val renderFn: js.ThisFunction2[js.Dynamic, js.Dynamic, js.Dynamic, js.Any] = (thiz, p, _) =>
      val raw   = if p == null || js.isUndefined(p) then thiz.props else p
      val props = read(raw)
      c.addSelectors(c.render(props), inst(thiz)).value
    proto.render = renderFn
    val should: js.ThisFunction3[js.Dynamic, js.Dynamic, js.Dynamic, js.Dynamic, Boolean] = (thiz, nextProps, _, _) =>
      c.shouldUpdate(read(nextProps), inst(thiz))
    proto.shouldComponentUpdate = should
    val didMount: js.ThisFunction0[js.Dynamic, Unit] = thiz =>
      c.willMount(inst(thiz))
      c.didMount(inst(thiz))
    proto.componentDidMount = didMount
    val willUn: js.ThisFunction0[js.Dynamic, Unit] = thiz => c.willUnMount(inst(thiz))
    proto.componentWillUnmount = willUn
    val didCatch: js.ThisFunction1[js.Dynamic, js.Error, Unit] = (thiz, e) => c.didCatch(e, inst(thiz))
    proto.componentDidCatch = didCatch
    ctor.asInstanceOf[js.Dynamic].displayName = c.classForClass
    ctor
  end statelessClass

  private def statefulClass[P, S](c: StatefulComponent[P, S], read: js.Dynamic => P): js.Dynamic =
    val base = HostClass.requireClass
    val ctor: js.ThisFunction1[js.Any, js.Dynamic, Unit] = (thiz, rawProps) =>
      base.call(thiz, rawProps)
      val fromThis = thiz.asInstanceOf[js.Dynamic].props
      val src =
        if rawProps == null || js.isUndefined(rawProps) then fromThis
        else rawProps
      val props = read(src)
      thiz.asInstanceOf[js.Dynamic].state =
        js.Dictionary(StateFieldName -> c.initialState(props).asInstanceOf[js.Any]).asInstanceOf[js.Dynamic]
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
      c.addSelectors(c.render(props, state, i), i).value
    proto.render = renderFn

    val should: js.ThisFunction3[js.Dynamic, js.Dynamic, js.Dynamic, js.Dynamic, Boolean] =
      (thiz, nextProps, nextState, _) => c.shouldUpdate(read(nextProps), Host.scalaState[S](nextState), inst(thiz))
    proto.shouldComponentUpdate = should

    val didMount: js.ThisFunction0[js.Dynamic, Unit] = thiz =>
      c.willMount(inst(thiz))
      c.didMount(inst(thiz))
    proto.componentDidMount = didMount

    val willUn: js.ThisFunction0[js.Dynamic, Unit] = thiz => c.willUnMount(inst(thiz))
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

    val gdsfp: js.Function2[js.Dynamic, js.Dynamic, js.Any] = (nextProps, prevState) =>
      val oldS = Host.scalaState[S](prevState)
      val next = c.deriveState(read(nextProps), oldS)
      if next == oldS then null
      else js.Dictionary(StateFieldName -> next.asInstanceOf[js.Any])
    ctorDyn.getDerivedStateFromProps = gdsfp

    val didCatch: js.ThisFunction1[js.Dynamic, js.Error, Unit] = (thiz, e) => c.didCatch(e, inst(thiz))
    proto.componentDidCatch = didCatch
    ctorDyn.displayName = c.classForClass
    ctorDyn
  end statefulClass

  class DummyInstance[P](p: P) extends js.Object with Instance[P, Nothing]:
    @scala.scalajs.js.annotation.JSName("eProps")
    def props: P = p
    @scala.scalajs.js.annotation.JSName("eState")
    def state: Nothing = throw IllegalStateException("stateless")
    @scala.scalajs.js.annotation.JSName("eSetState")
    def setState(s: Nothing): Unit = ()
    @scala.scalajs.js.annotation.JSName("eBase")
    def base: js.UndefOr[org.scalajs.dom.Element] = js.undefined
  end DummyInstance
end Adapters
