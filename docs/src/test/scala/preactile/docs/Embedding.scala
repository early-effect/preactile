package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Embedding extends DocSpecSuite:
  def doc = page("Embedding")(
    md"""
# Embedding

Preactile can render through a **host** React 18 or Preact tree. The host owns
`createElement`. You do not call `preactile.preact.render` on those pages.

Live demos, simple components and a conduit todo:

- [Embedding in React](/EmbeddingInReact): React 18 UMD as the host
- [Embedding in Preact](/EmbeddingInPreact): spliced Preact as the host (`Host.usePreactHost`)

Owned apps still use [Mounting](/Mounting) (`preactile.preact.render`).
"""
  )
end Embedding
