package preactile

import org.scalajs.dom

trait InstanceDataSelector:
  comp: PreactileComponent[?, ?] =>
  val attributeName = s"data-preactile-$classForClass"
  def extractAttributeValue(instance: comp.Instance): String
  def selector(attributeValue: String) = s"[$attributeName='$attributeValue']"

  def addDataAttribute(e: dom.Element, instance: Instance): Unit =
    e.setAttribute(attributeName, comp.extractAttributeValue(instance))
end InstanceDataSelector
