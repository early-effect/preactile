package preactile.docs

import specular.client.SpecularClient
import zio.*

/** Browser entry point for the docs site. */
object ClientMain extends ZIOAppDefault:

  val mounters = Map(
    "overview-greeting"   -> OverviewDemo.mounter,
    "stateful-counter"    -> CounterDemo.mounter,
    "components-demo"     -> ComponentsDemo.mounter,
    "element-dsl-demo"    -> ElementDslDemo.mounter,
    "styling-demo"        -> StylingDemo.mounter,
    "conduit-todo"        -> TodoDemo.mounter,
    "embed-react-simple"  -> EmbedReactDemo.simpleMounter,
    "embed-react-todo"    -> EmbedReactDemo.todoMounter,
    "embed-preact-simple" -> EmbedPreactDemo.simpleMounter,
    "embed-preact-todo"   -> EmbedPreactDemo.todoMounter,
  )

  def run = ZIO.scoped {
    // SpecularClient.mountAll already calls ascent.js.DevReload.install() (SSE on localhost).
    SpecularClient.mountAll(mounters) *> ZIO.never
  }

end ClientMain
