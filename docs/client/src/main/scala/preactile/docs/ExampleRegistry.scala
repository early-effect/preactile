package preactile.docs

import preactile.VNode

/** Collects interactive example components keyed by stable DOM ids. */
object ExampleRegistry:

  val examples: Map[String, VNode] = Vector(
    "basic-components-demo" -> InteractiveExamples.basicComponentsDemo(),
    "counter-demo"          -> InteractiveExamples.counterDemo(),
    "conditional-demo"      -> InteractiveExamples.conditionalDemo(),
    "list-demo"             -> InteractiveExamples.listDemo(),
  ).toMap

end ExampleRegistry
