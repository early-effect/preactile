package todo.model

import org.scalajs.dom
import scala.scalajs.js.typedarray.Uint8Array

case class Todo(key: String, description: String, complete: Boolean, editing: Boolean)

object Todo:

  def apply(description: String) = new Todo(randomUUID, description, complete = false, editing = false)

  /** Browser-native UUID v4 via Web Crypto. `java.util.UUID.randomUUID()` on Scala.js is backed by
    * scalajs-java-securerandom, which imports the Node `crypto` builtin — a specifier sbt-splice cannot resolve in a
    * self-contained browser bundle.
    */
  private def randomUUID: String =
    val b = new Uint8Array(16)
    dom.crypto.getRandomValues(b)
    b(6) = ((b(6) & 0x0f) | 0x40).toByte
    b(8) = ((b(8) & 0x3f) | 0x80).toByte
    val h = (0 until 16).map(i => f"${b(i).toInt}%02x").mkString
    s"${h.substring(0, 8)}-${h.substring(8, 12)}-${h.substring(12, 16)}-${h.substring(16, 20)}-${h.substring(20, 32)}"
  end randomUUID
end Todo
