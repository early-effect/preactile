package preactile

import scala.scalajs.js

import org.scalajs.dom
import org.scalajs.dom.Element

import preactile.dsl.css.Styles.DeclarationOrSelector
import preactile.dsl.css.Styles.KeyFrames
import preactile.dsl.css.Styles.MediaQuery
import preactile.impl.VNodeJS

sealed trait Arg:
  self =>
  def when(pred: => Boolean) = if pred then self else Empty

case object Empty extends Arg

trait Child extends Arg:
  self =>
  def value: js.Any
  override def when(pred: => Boolean): Child = if pred then self else EmptyChild

trait VNode extends Child:

  def withT[T <: js.Any](name: String, t: T): VNode =
    val props = vNode.props.asInstanceOf[js.Dictionary[js.Any]]
    vNode.ref.foreach(props.update("ref", _))
    vNode.key.foreach(props.update("key", _))
    props.update(name, t)
    Host.h(
      vNode.`type`.asInstanceOf[js.Any],
      props,
      vNode.rawChildren,
    )
  end withT

  def children: js.Array[VNode] = vNode.childArray.map(x => x: VNode)

  def withKey(key: String): VNode = withT(name = "key", key.asInstanceOf[js.Any])

  def withRef(f: js.Function1[dom.Element, Unit]): VNode =

    val combined: js.Function1[Element, Unit] = vNode.ref.fold(f)(existing =>
      (e: dom.Element) =>
        existing(e)
        f(e)
    )

    val safe: js.Function1[js.Any, Unit] = {
      case null           => ()
      case e: dom.Element => combined(e)
      case x: js.Any =>
        Option(x.asInstanceOf[js.Dynamic].base).map(_.asInstanceOf[dom.Element]).foreach(combined)
    }
    withT(name = "ref", safe)
  end withRef

  def value: js.Any = vNode
  def vNode: VNodeJS
  def rawChildren: js.Any           = vNode.props.children
  def childArray: js.Array[VNodeJS] = Host.toChildArray(rawChildren)

end VNode

object VNode:
  given Conversion[VNode, VNodeJS] = _.vNode

object Child:
  given Conversion[Option[Child], Child] = _.getOrElse(EmptyChild)
  given Conversion[Child, js.Any]        = _.value

case object EmptyChild extends Child:
  override def value: js.Any = null

final case class HostChild(raw: js.Any) extends Child:
  override def value: js.Any = raw

trait Attribute extends Arg:
  def name: String
  def value: js.Any

  override def toString: String = s"$name: ${value.toString}"

final case class SimpleAttribute(name: String, value: js.Any) extends Attribute

object Attribute:
  def apply(name: String, value: js.Any): Attribute = SimpleAttribute(name, value)

final case class Declaration(property: String, value: String) extends Arg with DeclarationOrSelector:

  override def mkString(className: String, kf: js.Array[KeyFrames], mq: js.Array[MediaQuery]): String =
    s"$property: $value;"

  def important: Declaration = copy(value = s"$value !important")

final case class StringArg(s: String) extends Child:
  override def value: js.Any = s

final case class DoubleArg(d: Double) extends Child:
  override def value: js.Any = d.toString

object Arg:
  given Conversion[String, Arg]        = StringArg(_)
  given Conversion[Double, Arg]        = DoubleArg(_)
  given Conversion[Int, Arg]           = DoubleArg(_)
  given Conversion[Option[Arg], Arg]   = _.getOrElse(Empty)
  given Conversion[Seq[Arg], Arg]      = Args(_)
  given Conversion[js.Array[Arg], Arg] = Args(_)

case class Args(args: Seq[Arg]) extends Arg:

  lazy val attributeDictionary =
    js.Dictionary(normalizeStyles(normalizeClasses(attributes)).map(x => x.name -> x.value).toSeq*)

  def normalizeClasses(attrs: js.Array[Attribute]): js.Array[Attribute] =
    val classes = attrs.filter(_.name == "class")
    if classes.length <= 1 then attrs
    else
      val combinedClass = A.`class`(classes.map(x => x.value).mkString(" "))
      attrs.filterNot(x => classes.contains(x)) :+ combinedClass

  def normalizeStyles(attrs: js.Array[Attribute]): js.Array[Attribute] =
    val styles = attributes.filter(_.name == "style")
    if styles.length <= 1 then attrs
    else attrs.filterNot(x => styles.contains(x)) :+ combineStyles(styles.toSeq*)

  def combineStyles(attrs: Attribute*): Attribute =
    val d = js.Dictionary[String]()
    attrs.foreach { a =>
      a.value.asInstanceOf[js.Dictionary[String]].foreach(x => d.update(x._1, x._2))
    }
    Attribute("style", d)

  lazy val attributes: js.Array[Attribute] =
    val as = js.Array[Attribute]()
    val ds = js.Array[Declaration]()
    if args != null then
      args.foreach {
        case a: Attribute   => as.push(a)
        case na: Args       => as.push(na.attributes.toSeq*)
        case d: Declaration => ds.push(d)
        case _              =>
      }
      if ds.nonEmpty then as.push(A.style(ds.toSeq*))
    as
  end attributes

  lazy val children: js.Array[Child] =
    val cs = js.Array[Child]()
    if args != null then
      args.foreach {
        case c: Child => cs.push(c)
        case na: Args => cs.push(na.children.toSeq*)
        case null     => cs.push(EmptyChild)
        case _        =>
      }
    cs
  end children
end Args

object Args:
  def apply[A <: Arg](args: js.Array[A]) = new Args(args.toSeq)
