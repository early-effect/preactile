package todo

import org.scalajs.dom.document
import todo.model.TodosConduit

import zio.*

import ascent.js.DevReload
import preactile.*

object Main extends ZIOAppDefault:
  def run =
    for
      _ <- ZIO.attempt:
        val e = document.createElement("body")
        document.documentElement.replaceChild(e, document.body)
        preactile.preact.render(App.component, document.documentElement, e)
      // Localhost-only live reload: ascent-preview pushes on assets/dev-stamp change.
      _ <- ZIO.succeed(DevReload.install())
      c <- TodosConduit.run(false)
    yield c
  end run
end Main
