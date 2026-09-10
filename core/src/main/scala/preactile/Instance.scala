package preactile

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

import org.scalajs.dom.Element

/** Type-safe view of a mounted component instance. */
trait Instance[Props, State] extends js.Any:

  @JSName("eProps")
  def props: Props

  @JSName("eState")
  def state: State

  @JSName("eSetState")
  def setState(s: State): Unit

  @JSName("eBase")
  def base: UndefOr[Element]
end Instance
