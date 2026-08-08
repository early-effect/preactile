package preactile

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

import preactile.impl.VNodeJS

trait StatefulComponent[Props, State] extends PreactileComponent[Props, State]:
  theComponent =>
  def initialState(props: Props): State

  def deriveState(props: Props, oldState: State) = oldState

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    previous.state != nextState || previous.props != nextProps

  def render(props: Props, state: State, instance: Instance): VNode

  override lazy val instanceConstructor: js.Dynamic = js.constructorOf[StatefulInstance]

  final private class StatefulInstance extends InstanceFacade[Props, State]:

    // Track whether state has been initialized. componentWillMount can't read props in newer Preact,
    // so we initialize lazily in renderJS where the props argument is available.
    private var _stateInitialized = false

    override def componentDidMount(): Unit = didMount(this)

    override def componentWillUnmount(): Unit = willMount(this)

    @JSName("render")
    override def renderJS(p: js.Dynamic, s: js.Dynamic): VNodeJS =
      val componentProps = lookupProps(p)
      if !_stateInitialized then
        val initState = theComponent.initialState(componentProps)
        setState(initState)
        _stateInitialized = true
        addSelectors(render(componentProps, initState, instance = this), this)
      else
        val currentState = lookupState(s)
        addSelectors(render(componentProps, currentState, instance = this), this)

    override def shouldComponentUpdate(nextProps: js.Dynamic, nextState: js.Dynamic, nextContext: js.Dynamic): Boolean =
      theComponent.shouldUpdate(lookupProps(nextProps), lookupState(nextState), previous = this)

    override def componentDidUpdate(oldProps: js.Dynamic, oldState: js.Dynamic, snapshot: js.Dynamic): Unit =
      theComponent.didUpdate(
        lookupProps(oldProps),
        lookupState(oldState),
        instance = this,
        snapshot.asInstanceOf[UndefOr[StatefulComponent[Props, State]#Instance]],
      )

    override def componentWillReceiveProps(nextProps: js.Dynamic, nextContext: js.Dynamic): Unit =
      val res = theComponent.deriveState(lookupProps(nextProps), lookupState())
      setState(res)

    // Don't read props here: newer Preact doesn't set this.props before componentWillMount.
    // Initial state is computed lazily in renderJS where props are available as arguments.
    override def componentWillMount(): Unit = ()

    override def componentDidCatch(e: js.Error): Unit =
//      log("caught", e)
      didCatch(e, this)
  end StatefulInstance
end StatefulComponent

object StatefulComponent:
  given selfComp[Comp <: StatefulComponent[Comp, ?], T <: Arg]: Conversion[Comp, T] = c => c.apply(c).asInstanceOf[T]
