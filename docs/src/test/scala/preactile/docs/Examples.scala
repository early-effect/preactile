package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object Examples extends DocSpecSuite:
  def doc = page("Examples")(
    md"""
       # Examples

       The live interactive demos are embedded on their respective pages:

       - **Overview**: A basic stateless greeting component showing how components render.
       - **Stateful Components**: An interactive counter demonstrating `StatefulComponent`,
         `initialState`, and `setState`.

       For more code examples, see the [Components](/Components) and [Element DSL](/ElementDsl) pages.
       """
  )
end Examples
