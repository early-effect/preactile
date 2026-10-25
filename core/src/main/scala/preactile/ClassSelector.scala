package preactile

import org.scalajs.dom

trait ClassSelector:
  comp: PreactileComponent[?, ?] =>
  def selector = s".${comp.classForClass}"

  def addClass(e: dom.Element): Unit = if !e.classList.contains(classForClass) then e.classList.add(classForClass)
end ClassSelector

object ClassSelector:

  def makeCssClass(className: String): String =
    val res = className.replaceAll("[^\\w]", "-")
    if res.endsWith("-") then res.dropRight(1) else res
end ClassSelector
