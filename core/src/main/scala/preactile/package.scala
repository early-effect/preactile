package preactile

import scala.scalajs.js

import org.scalajs.dom

import preactile.dsl.*
import preactile.dsl.css.Styles

import scala.scalajs.js.JSConverters.*

type ComponentFunction[T] = T => VNode

type A = Attributes.type
val A: A = Attributes

type E = Elements.type
val E: E = Elements

type S = Styles.type
val S: Styles.type = Styles

def log(m: js.Any, a: Any*): Unit = dom.window.console.log(m, a.map(_.asInstanceOf[js.Any])*)

def fragment(children: Child*): VNode = impl.Preactile.h(Host.current.fragment, null, children.toJSArray)

def when(p: => Boolean) = When(p)

def text(s: String) = StringArg(s)

extension (d: Double)
  def pct = s"$d%"
  def px  = s"${d}px"
  def em  = s"${d}em"

extension (n: Int)
  def pct = s"$n%"
  def px  = s"${n}px"
  def em  = s"${n}em"
def args(as: Arg*) = Args(as)

object dictionaryNames:
  val PropsFieldName = "_preactile_props"
  val StateFieldName = "_preactile_state"
