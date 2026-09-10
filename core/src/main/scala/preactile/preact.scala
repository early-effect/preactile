package preactile

import scala.scalajs.js

import org.scalajs.dom

import preactile.impl.Preact
import preactile.impl.Preact.AnyDictionary
import preactile.impl.Preact.ComponentChildren
import preactile.impl.VNodeJS

object preact:

  def render(node: => VNode, parent: dom.Element): Unit =
    ensureInstalled()
    Preact.render(node.vNode, parent)

  def render(node: => VNode, parent: dom.Element, replaceNode: dom.Element): Unit =
    ensureInstalled()
    Preact.render(node.vNode, parent, replaceNode)

  def rerender(): Unit =
    ensureInstalled()
    Preact.rerender()

  private[preactile] def ensureInstalled(): Unit =
    if !Host.isInstalled then installPreact()
    else if Host.kind != HostKind.Preact then
      throw IllegalStateException("preact.render cannot run after Host.use installed a non-Preact renderer.")

  private def installPreact(): Unit =
    Host.install(
      HostRuntime(
        kind = HostKind.Preact,
        createElement = preactCreateElement,
        fragment = Preact.Fragment,
        toChildArray = (children: js.Any) =>
          Preact.toChildArray(children.asInstanceOf[ComponentChildren]).asInstanceOf[js.Array[js.Any]],
        hooks = HostHooks.empty,
        componentClass = Preact.Component,
        typeFor = _.buildHostType(),
        epoch = 0,
      )
    )

  private val preactCreateElement: js.Function3[js.Any, js.Any, js.Any, VNodeJS] =
    (`type`: js.Any, params: js.Any, children: js.Any) =>
      val dict   = params.asInstanceOf[AnyDictionary]
      val noKids = children == null || js.isUndefined(children)
      val kids   = children.asInstanceOf[ComponentChildren]
      if js.typeOf(`type`) == "string" then
        val s = `type`.asInstanceOf[String]
        if noKids then Preact.h(s, dict) else Preact.h(s, dict, kids)
      else
        val d = `type`.asInstanceOf[js.Dynamic]
        if noKids then Preact.h(d, dict) else Preact.h(d, dict, kids)
end preact
