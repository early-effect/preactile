package preactile.docs

import org.scalajs.dom.document
import zio.*

/** Browser entry: mount each interactive example into its DOM wrapper by id. */
object ClientMain extends ZIOAppDefault:

  def run =
    for
      _ <- ZIO.attempt {
        ExampleRegistry.examples.foreach { (id, component) =>
          val el = document.getElementById(id)
          if el != null then preactile.preact.render(component, el)
        }
      }
      _ <- ZIO.never // keep the app alive
    yield ()
  end run

end ClientMain
