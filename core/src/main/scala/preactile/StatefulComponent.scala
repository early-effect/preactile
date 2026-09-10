package preactile

import scala.scalajs.js

trait StatefulComponent[Props, State] extends PreactileComponent[Props, State]:
  theComponent =>
  def initialState(props: Props): State

  def deriveState(props: Props, oldState: State) = oldState

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    previous.state != nextState || previous.props != nextProps

  def render(props: Props, state: State, instance: Instance): VNode

  override def embedType: js.Dynamic = host.Adapters.statefulType(theComponent)
end StatefulComponent

object StatefulComponent:
  given selfComp[Comp <: StatefulComponent[Comp, ?], T <: Arg]: Conversion[Comp, T] = c => c.apply(c).asInstanceOf[T]
