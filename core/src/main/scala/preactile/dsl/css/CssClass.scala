package preactile.dsl.css
import scala.scalajs.js

import org.scalajs.dom

import preactile.Attribute
import preactile.ClassSelector
import preactile.dsl.css.Styles.DeclarationOrSelector
import preactile.dsl.css.Styles.KeyFrames
import preactile.dsl.css.Styles.MediaQuery
import preactile.dsl.css.Styles.Selector

abstract class CssClass(members: DeclarationOrSelector*) extends Attribute:
  self =>

  private val className: String = ClassSelector.makeCssClass(self.getClass.getName)
  private val selector          = s".$className"
  private val sel: Selector     = Selector(selector, members*)

  override val name          = "class"
  override val value: js.Any = className

  private def mkString: String =
    val keyframes    = js.Array[KeyFrames]()
    val mediaQueries = js.Array[MediaQuery]()
    val mainCss      = sel.mkString(className, keyframes, mediaQueries)
    val keyFramesCss = keyframes
      .map { k =>
        Selector(s"@keyframes $className-${k.name}", k.selectors*)
      }
      .mkString("\n")
    val mediaQueriesCss = mediaQueries.map(_.render).mkString("\n")
    mainCss + keyFramesCss + mediaQueriesCss

  private def appendStyle(): Unit =
    val d                  = org.scalajs.dom.document
    val style: dom.Element = d.createElement("style")
    style.setAttribute("data-style-for", className)
    style.appendChild(d.createTextNode(mkString))
    Option(d.head.querySelector(s"[data-style-for='$className']")).fold(d.head.appendChild(style))(e =>
      d.head.replaceChild(style, e)
    )
  self.appendStyle()
end CssClass
