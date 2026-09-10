package preactile

import scala.scalajs.js

import org.scalajs.dom.Element

import preactile.impl.VNodeJS

enum HostKind:
  case Preact, React

final class HostHooks(
    val useState: js.UndefOr[js.Function] = js.undefined,
    val useEffect: js.UndefOr[js.Function] = js.undefined,
    val useRef: js.UndefOr[js.Function] = js.undefined,
    val useReducer: js.UndefOr[js.Function] = js.undefined,
):
  def isDefined: Boolean = useState.isDefined && useEffect.isDefined
end HostHooks

object HostHooks:
  val empty: HostHooks = HostHooks()

  /** `react` or `preact/hooks` namespace (`useState`, `useEffect`, `useRef`). */
  def from(namespace: js.Dynamic): HostHooks =
    HostHooks(
      useState = asFun(namespace.useState),
      useEffect = asFun(namespace.useEffect),
      useRef = asFun(namespace.useRef),
      useReducer = asFun(namespace.useReducer),
    )

  private def asFun(value: js.Dynamic): js.UndefOr[js.Function] =
    if value == null || js.isUndefined(value) then js.undefined
    else value.asInstanceOf[js.Function]
end HostHooks

final class HostRuntime(
    val kind: HostKind,
    val createElement: js.Function,
    val fragment: js.Any,
    val toChildArray: js.Function1[js.Any, js.Array[js.Any]],
    val hooks: HostHooks,
    val componentClass: js.UndefOr[js.Dynamic],
    val typeFor: PreactileComponent[?, ?] => js.Dynamic,
    val epoch: Int,
):
  private val types = scala.collection.mutable.HashMap.empty[PreactileComponent[?, ?], js.Dynamic]

  def componentType(c: PreactileComponent[?, ?]): js.Dynamic =
    types.getOrElseUpdate(c, typeFor(c))
end HostRuntime

/** Instance wrapping a host class-component `this` (`React.Component` or `preact.Component`). */
class HostInstance[P, S](
    val raw: js.Dynamic,
    readProps: js.Dynamic => P,
    readState: js.Dynamic => S,
) extends js.Object
    with Host.Component[P, S]:
  @scala.scalajs.js.annotation.JSName("eProps")
  def props: P = readProps(raw.props)
  @scala.scalajs.js.annotation.JSName("eState")
  def state: S = readState(raw.state)
  @scala.scalajs.js.annotation.JSName("eSetState")
  def setState(s: S): Unit =
    raw.setState(js.Dictionary(dictionaryNames.StateFieldName -> s.asInstanceOf[js.Any]))
  @scala.scalajs.js.annotation.JSName("eBase")
  def base: js.UndefOr[Element] =
    val b = raw.base
    if b == null || js.isUndefined(b) then js.undefined else b.asInstanceOf[Element]
  def render(): VNode = throw IllegalStateException("Host.Component.render is implemented on the JS prototype")
end HostInstance

object Host:
  /** Contract for a host class-component instance. Adapters extend the `componentClass` passed to [[use]]
    * (`React.Component` or `preact.Component`) so embedders can avoid function components entirely. `setState` and
    * lifecycle then follow the host.
    */
  trait Component[P, S] extends Instance[P, S]:
    def render(): VNode

  private var installed: HostRuntime | Null = null
  private var epoch: Int                    = 0

  def isInstalled: Boolean = installed != null

  def kind: HostKind = current.kind

  def current: HostRuntime =
    val h = installed
    if h == null then
      throw IllegalStateException(
        "No renderer installed. Call Host.use(...) before building VNodes, or preactile.preact.render for owned Preact apps."
      )
    h

  private[preactile] def reset(): Unit =
    installed = null
    epoch += 1

  private[preactile] def install(runtime: HostRuntime): Unit =
    val existing = installed
    if existing != null && (existing.kind != runtime.kind) then
      throw IllegalStateException(
        s"Host already installed as ${existing.kind}; cannot install ${runtime.kind}."
      )
    installed = runtime

  def use(
      createElement: js.Function,
      fragment: js.Any,
      toChildArray: js.UndefOr[js.Function1[js.Any, js.Array[js.Any]]] = js.undefined,
      hooks: HostHooks = HostHooks.empty,
      componentClass: js.UndefOr[js.Dynamic] = js.undefined,
      kind: HostKind = HostKind.React,
  ): Unit =
    val tca = toChildArray.getOrElse(defaultToChildArray)
    epoch += 1
    install(
      HostRuntime(
        kind = kind,
        createElement = createElement,
        fragment = fragment,
        toChildArray = tca,
        hooks = hooks,
        componentClass = componentClass,
        typeFor = _.buildHostType(),
        epoch = epoch,
      )
    )
  end use

  def useReact(react: js.Dynamic): Unit =
    val children = react.Children
    val toArray: js.Function1[js.Any, js.Array[js.Any]] =
      if children == null || js.isUndefined(children) then defaultToChildArray
      else
        val fn = children.toArray
        if fn == null || js.isUndefined(fn) then defaultToChildArray
        else (c: js.Any) => fn.call(children, c).asInstanceOf[js.Array[js.Any]]
    use(
      createElement = react.createElement.asInstanceOf[js.Function],
      fragment = react.Fragment,
      toChildArray = toArray,
      hooks = HostHooks.from(react),
      componentClass = asDyn(react.Component),
      kind = HostKind.React,
    )
  end useReact

  def usePreactHost(preactNs: js.Dynamic, hooksNs: js.UndefOr[js.Dynamic] = js.undefined): Unit =
    val hooks = hooksNs.fold(HostHooks.empty)(HostHooks.from)
    use(
      createElement = preactNs.h.asInstanceOf[js.Function],
      fragment = preactNs.Fragment,
      toChildArray = preactNs.toChildArray.asInstanceOf[js.Function1[js.Any, js.Array[js.Any]]],
      hooks = hooks,
      componentClass = asDyn(preactNs.Component),
      kind = HostKind.Preact,
    )
  end usePreactHost

  def h(`type`: js.Any, params: js.Dictionary[js.Any] | Null, children: js.Array[Child]): VNodeJS =
    call(`type`, mapParams(params), children.map(_.value))

  def h(`type`: js.Any, params: js.Dictionary[js.Any] | Null): VNodeJS =
    call(`type`, mapParams(params), null)

  def h(`type`: js.Any, params: js.Dictionary[js.Any] | Null, rawChildren: js.Any): VNodeJS =
    call(`type`, mapParams(params), rawChildren)

  def toChildArray(children: js.Any): js.Array[VNodeJS] =
    current.toChildArray(children).asInstanceOf[js.Array[VNodeJS]]

  def element[P, S](c: PreactileComponent[P, S], props: P): VNodeJS =
    h(current.componentType(c), c.baseDictionary(props))

  private[preactile] def scalaProps[P](raw: js.Dynamic): P =
    if raw == null || js.isUndefined(raw) then ().asInstanceOf[P]
    else raw.selectDynamic(dictionaryNames.PropsFieldName).asInstanceOf[P]

  private[preactile] def scalaState[S](raw: js.Dynamic): S =
    if raw == null || js.isUndefined(raw) then null.asInstanceOf[S]
    else raw.selectDynamic(dictionaryNames.StateFieldName).asInstanceOf[S]

  private def asDyn(value: js.Dynamic): js.UndefOr[js.Dynamic] =
    if value == null || js.isUndefined(value) then js.undefined else value

  private def call(`type`: js.Any, params: js.Any, children: js.Any): VNodeJS =
    val ce = current.createElement.asInstanceOf[js.Dynamic]
    val result =
      if children == null || js.isUndefined(children) then ce.call(js.undefined, `type`, params)
      else if current.kind == HostKind.React && js.Array.isArray(children) then
        val arr  = children.asInstanceOf[js.Array[js.Any]]
        val args = js.Array[js.Any](`type`.asInstanceOf[js.Any], params.asInstanceOf[js.Any])
        var i    = 0
        while i < arr.length do
          args.push(arr(i))
          i += 1
        ce.applyDynamic("apply")(js.undefined, args)
      else ce.call(js.undefined, `type`, params, children)
    result.asInstanceOf[VNodeJS]
  end call

  private def mapParams(params: js.Dictionary[js.Any] | Null): js.Any =
    if params == null then params
    else if current.kind != HostKind.React then params
    else
      val out = js.Dictionary[js.Any]()
      params.foreach { case (k, v) =>
        out.update(reactPropName(k), v)
      }
      out

  private def reactPropName(name: String): String =
    name match
      case "class" => "className"
      case "for"   => "htmlFor"
      case n if n.startsWith("on") && n.length > 2 && n.charAt(2).isLower =>
        "on" + n.charAt(2).toUpper + n.substring(3)
      case other => other

  private[preactile] val defaultToChildArray: js.Function1[js.Any, js.Array[js.Any]] =
    (children: js.Any) =>
      if children == null || js.isUndefined(children) then js.Array[js.Any]()
      else if js.Array.isArray(children) then children.asInstanceOf[js.Array[js.Any]]
      else js.Array(children)
end Host
