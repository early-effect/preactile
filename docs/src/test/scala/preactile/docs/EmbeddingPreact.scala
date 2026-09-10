package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object EmbeddingPreact extends DocSpecSuite:
  def doc = page("Embedding in Preact")(
    md"""
# Embedding in host Preact

This page is a **host-owned** Preact tree (`Host.usePreactHost`). Preactile
components are exported the same way as on the React page and mounted with
`preact.h` into the SSR placeholders below. That is different from
`preactile.preact.render`, which is the owned-app path.

See [Embedding in React](/EmbeddingInReact) for the React 18 version.

## Install the host once

```scala
import scala.scalajs.js
import preactile.Host

Host.usePreactHost(js.Dynamic.global.preact)
```

## Simple: greeting and a stateful counter
""",
    exampleDom("embed-preact-simple").fromSource(
      "docs/client/src/main/scala/preactile/docs/EmbedDemos.scala",
      "simple",
    ),
    md"""
## Complex: conduit todo

Same todo UI, host Preact as the renderer.
""",
    exampleDom("embed-preact-todo").fromSource(
      "docs/client/src/main/scala/preactile/docs/EmbedDemos.scala",
      "todo",
    ),
  )
end EmbeddingPreact
