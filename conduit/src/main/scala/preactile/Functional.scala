package preactile

import conduit.*

private class Functional[Props, Model <: Product: Optics as m, Event, State](
    conduit: Conduit[Model, Event],
    lensF: Optics[Model] => Lens[Model, State] = identity,
    renderF: (Props, State) => VNode,
) extends ConduitComponent[Props, Model, Event, State](conduit, lensF):
  override def render(props: Props, state: State): VNode = renderF(props, state)

extension [Model <: Product: Optics as model, Event](conduit: Conduit[Model, Event])
  def component[Props, State](
      lens: Optics[Model] => Lens[Model, State]
  )(render: (Props, State) => VNode): ConduitComponent[Props, Model, Event, State] =
    Functional(conduit, lens, render)

  def component[Props](
      render: (Props, Model) => VNode
  ): ConduitComponent[Props, Model, Event, Model] =
    Functional(conduit, identity, render)

  def component(
      render: Model => VNode
  ): ConduitComponent[Unit, Model, Event, Model] =
    Functional(conduit, identity, (_, m) => render(m))

  inline def component[Props, State](
      inline path: Model => State
  )(render: (Props, State) => VNode): Functional[Props, Model, Event, State] =
    Functional(
      conduit,
      (m: Optics[Model]) => m(path),
      render,
    )

  inline def component[State](
      inline path: Model => State
  )(render: State => VNode): Functional[Any, Model, Event, State] =
    Functional(
      conduit,
      (m: Optics[Model]) => m(path),
      (_, m) => render(m),
    )

end extension
