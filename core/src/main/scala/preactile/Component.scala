package preactile

import scala.scalajs.js
import scala.scalajs.js.UndefOr

trait Component[Props] extends PreactileComponent[Props, Nothing]:
  theComponent =>

  def render(props: Props): VNode

  // noinspection ScalaUnusedSymbol
  def didUpdate(oldProps: Props, instance: Instance, oldInstance: UndefOr[Instance]): Unit = ()

  def shouldUpdate(nextProps: Props, previous: Instance): Boolean = nextProps != previous.props

  override def embedType: js.Dynamic = host.Adapters.statelessType(theComponent)
end Component

object Component:

  given selfComp[Comp <: Component[Comp], T <: Arg]: Conversion[Comp, T] = c => c.apply(c).asInstanceOf[T]
  def apply[Props](renderF: ComponentFunction[Props]): Component[Props] =
    new FunctionalComponent(renderF)

class FunctionalComponent[Props](renderF: ComponentFunction[Props]) extends Component[Props]:
  override def render(props: Props): VNode = renderF(props)
given Conversion[ComponentFunction[?], Component[?]] = (f: ComponentFunction[?]) => Component(f)
