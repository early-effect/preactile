package preactile.docs

import org.scalajs.dom.{Event => DomEvent}

import preactile.*
import preactile.dsl.css.*

/** Interactive demo components mounted into the docs site. */
object InteractiveExamples:

  import S.*

  // --- Basic Components Demo ---

  object Greeting extends Component[Unit]:
    def render(props: Unit): VNode =
      E.div(
        margin.px(8),
        padding.px(12),
        background("#f0f4ff"),
        borderRadius.px(6),
        border("1px solid #d0d9f5"),
        E.h3(margin.zero, "Hello from preactile!"),
        E.p(marginTop.px(4), color("#555"), fontSize.px(14), "This is a live Preactile component."),
      )

  def basicComponentsDemo(): VNode = Greeting(())

  // --- Counter Demo (StatefulComponent) ---

  object Counter extends StatefulComponent[Unit, Int]:
    override def initialState(props: Unit): Int = 0

    def render(props: Unit, state: Int, instance: Counter.Instance): VNode =
      E.div(
        margin.px(8),
        padding.px(12),
        background("#f0fff4"),
        borderRadius.px(6),
        border("1px solid #c6f6d5"),
        display.flex,
        alignItems.center,
        gap("12px"),
        E.button(
          A.onClick(_ => instance.setState(state - 1)),
          padding("4px 10px"),
          fontSize.px(18),
          background("#e53e3e"),
          color("white"),
          border.none,
          borderRadius.px(4),
          cursor.pointer,
          "-",
        ),
        E.span(fontSize.px(20), fontWeight.bold, state.toString),
        E.button(
          A.onClick(_ => instance.setState(state + 1)),
          padding("4px 10px"),
          fontSize.px(18),
          background("#38a169"),
          color("white"),
          border.none,
          borderRadius.px(4),
          cursor.pointer,
          "+",
        ),
      )

  def counterDemo(): VNode = Counter(())

  // --- Conditional Rendering Demo ---

  object Toggle extends StatefulComponent[Unit, Boolean]:
    override def initialState(props: Unit): Boolean = false

    def render(props: Unit, state: Boolean, instance: Toggle.Instance): VNode =
      E.div(
        margin.px(8),
        padding.px(12),
        background("#fffaf0"),
        borderRadius.px(6),
        border("1px solid #feebc8"),
        E.button(
          A.onClick(_ => instance.setState(!state)),
          padding("4px 10px"),
          marginBottom.px(8),
          fontSize.px(14),
          background("#dd6b20"),
          color("white"),
          border.none,
          borderRadius.px(4),
          cursor.pointer,
          if state then "Hide" else "Show",
        ),
        when(state) {
          E.div(
            padding.px(8),
            background("#fefcbf"),
            borderRadius.px(4),
            fontSize.px(14),
            color("#744210"),
            "This content appears conditionally!",
          )
        },
      )

  def conditionalDemo(): VNode = Toggle(())

  // --- List Rendering Demo ---

  case class Item(name: String, done: Boolean)

  object TodoList extends StatefulComponent[Unit, List[Item]]:
    override def initialState(props: Unit): List[Item] = List(
      Item("Learn preactile", false),
      Item("Build something cool", false),
    )

    def render(props: Unit, state: List[Item], instance: TodoList.Instance): VNode =
      E.div(
        margin.px(8),
        padding.px(12),
        background("#faf5ff"),
        borderRadius.px(6),
        border("1px solid #e9d8fd"),
        fontSize.px(14),
        {
          val items = state.zipWithIndex.map { case (item, idx) =>
            E.li(
              display.flex,
              alignItems.center,
              gap("6px"),
              paddingTop.px(2),
              paddingBottom.px(2),
              textDecoration(if item.done then "line-through" else "none"),
              color(if item.done then "#a0aec0" else "#2d3748"),
              E.span(
                cursor.pointer,
                fontWeight.bold,
                A.onClick(_ => instance.setState(state.updated(idx, item.copy(done = !item.done)))),
                if item.done then "✓" else "○",
              ),
              item.name,
            )
          }.toList
          val allArgs: Seq[Arg] = Seq(margin.zero, paddingLeft.px(16)) ++ items
          E.ul(allArgs*)
        },
      )

  def listDemo(): VNode = TodoList(())

end InteractiveExamples
