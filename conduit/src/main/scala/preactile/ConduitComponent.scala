package preactile

import scala.scalajs.js

import conduit.*

abstract class ConduitComponent[Props, Model <: Product: Optics as m, Event, State](
    private[preactile] val conduit: Conduit[Model, Event],
    lensF: Optics[Model] => Lens[Model, State] = identity,
) extends PreactileComponent[Props, State]:
  theComponent =>
  val lens = lensF(m)
  def render(props: Props, state: State): VNode

  def shouldUpdate(nextProps: Props, nextState: State, previous: Instance): Boolean =
    previous.state != nextState || previous.props != nextProps

  override def embedType: js.Dynamic = host.ConduitAdapters.hostType(theComponent)
end ConduitComponent
