package preactile.docs

import specular.MountKey
import specular.client.SpecularClient
import zio.*

/** Browser entry point for the docs site. */
object ClientMain extends ZIOAppDefault:

  val mounters = Map(
    MountKey("overview-counter")    -> CounterDemo.mounter,
    MountKey("stateful-counter")    -> CounterDemo.mounter,
    MountKey("components-demo")     -> ComponentsDemo.mounter,
    MountKey("element-dsl-demo")    -> ElementDslDemo.mounter,
    MountKey("styling-demo")        -> StylingDemo.mounter,
    MountKey("conduit-todo")        -> TodoDemo.mounter,
    MountKey("embed-react-simple")  -> EmbedReactDemo.simpleMounter,
    MountKey("embed-react-todo")    -> EmbedReactDemo.todoMounter,
    MountKey("embed-preact-simple") -> EmbedPreactDemo.simpleMounter,
    MountKey("embed-preact-todo")   -> EmbedPreactDemo.todoMounter,
  )

  def run = ZIO.scoped {
    // SpecularClient.mountAll already calls ascent.js.DevReload.install() (SSE on localhost).
    SpecularClient.mountAll(mounters) *> ZIO.never
  }

end ClientMain
