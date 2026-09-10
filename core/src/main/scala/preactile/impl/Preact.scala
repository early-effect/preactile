package preactile.impl

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

import org.scalajs.dom

import preactile.*

abstract class AorC

@js.native
@JSImport("preact", JSImport.Namespace)
object Preact extends js.Object:

  val Fragment: js.Dynamic  = js.native
  val Component: js.Dynamic = js.native

  type AnyDictionary = js.Dictionary[js.Any]

  type AttributeOrChildJS = VNodeJS | Attribute | Declaration | String | Double | Args | js.Object

  type ComponentChild    = VNodeJS | js.Object | String | Double | Boolean
  type ComponentChildren = js.Array[ComponentChild] | ComponentChild

  type ChildJS = VNodeJS | String

  def toChildArray(children: ComponentChildren): js.Array[VNodeJS] = js.native

  def h(`type`: js.Dynamic, params: AnyDictionary, children: ComponentChildren): VNodeJS = js.native

  def h(`type`: String, params: AnyDictionary, children: ComponentChildren): VNodeJS = js.native

  def h(`type`: js.Dynamic, params: AnyDictionary): VNodeJS = js.native

  def h(`type`: String, params: AnyDictionary): VNodeJS = js.native

  def render(node: VNodeJS, parent: dom.Element): Unit = js.native

  def render(node: VNodeJS, parent: dom.Element, replaceNode: dom.Element): Unit = js.native

  def rerender(): Unit = js.native
end Preact
