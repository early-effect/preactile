package preactile.docs

import scala.scalajs.js.Math.random
import scala.scalajs.js.timers.setTimeout

import org.scalajs.dom.{HTMLInputElement, console}

import specular.client.Mounter

import conduit.*
import preactile.*
import preactile.dsl.css.*

/** Live todo demo for the Conduit Integration docs page.
  *
  * Shows off:
  *   - Conduit model/handler with Optics
  *   - ConduitComponent subscriptions (full model and lens-based)
  *   - Sending actions from event handlers
  *   - StatefulComponent for inline editing
  *   - Preactile CSS DSL styling that fits the docs theme
  */
object TodoDemo:

  // specular:begin demo-model
  // ---- Model & Handler ----

  case class Todo(key: String, text: String, done: Boolean, editing: Boolean) derives Optics

  private var _nextKey = 0L

  object Todo:
    def apply(text: String): Todo =
      _nextKey += 1
      new Todo(s"todo-${_nextKey}-${random()}", text, false, false)

  sealed trait Filter extends Product with Serializable
  object Filter:
    case object All       extends Filter
    case object Active    extends Filter
    case object Completed extends Filter

  case class TodoModel(items: List[Todo], filter: Filter) derives Optics

  sealed trait TodoAction extends Action
  object TodoActions:
    final case class Add(text: String)                        extends TodoAction
    final case class Toggle(key: String)                      extends TodoAction
    final case class Delete(key: String)                      extends TodoAction
    final case class SetFilter(f: Filter)                     extends TodoAction
    case object ClearCompleted                                extends TodoAction
    final case class SetAllDone(done: Boolean)                extends TodoAction
    final case class StartEditing(key: String)                extends TodoAction
    final case class FinishEditing(key: String, text: String) extends TodoAction
    final case class CancelEditing(key: String)               extends TodoAction
  end TodoActions

  val todoHandler = handle[TodoModel, TodoAction]:
    case TodoActions.Add(text) =>
      console.log(s"[TodoDemo] Handler: Add('$text')")
      update(m =>
        val next = m.copy(items = m.items :+ Todo(text))
        console.log(s"[TodoDemo] Handler: new model has ${next.items.size} items")
        next
      )
    case TodoActions.Toggle(key) =>
      update(m => m.copy(items = m.items.map(t => if t.key == key then t.copy(done = !t.done) else t)))
    case TodoActions.Delete(key) =>
      update(m => m.copy(items = m.items.filterNot(_.key == key)))
    case TodoActions.SetFilter(f) =>
      update(_.copy(filter = f))
    case TodoActions.ClearCompleted =>
      update(m => m.copy(items = m.items.filterNot(_.done)))
    case TodoActions.SetAllDone(done) =>
      update(m => m.copy(items = m.items.map(_.copy(done = done))))
    case TodoActions.StartEditing(key) =>
      update(m => m.copy(items = m.items.map(t => if t.key == key then t.copy(editing = true) else t)))
    case TodoActions.FinishEditing(key, text) =>
      update(m =>
        m.copy(items = m.items.map { t =>
          if t.key == key && text.nonEmpty then t.copy(text = text.trim, editing = false)
          else if t.key == key then t.copy(editing = false)
          else t
        })
      )
    case TodoActions.CancelEditing(key) =>
      update(m => m.copy(items = m.items.map(t => if t.key == key then t.copy(editing = false) else t)))

  // Create an isolated conduit instance for this demo.
  // Components subscribe via conduit.component{...} and send actions via conduit.unsafe(action).
  val todoConduit = Conduit.make(TodoModel(Nil, Filter.All))(todoHandler)
  // specular:end

  // specular:begin demo-css
  // ---- Styling with Preactile CSS DSL ----

  object styles:
    import S.*

    // Container that fits the docs theme nicely.
    // Cascade override: specular sets `.Content section > ul { max-width: 52rem }`.
    // `.App section > ul` matches that specificity and wins when our styles load after the theme.
    object App
        extends CssClass(
          fontFamily("system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif"),
          fontSize.px(14),
          lineHeight.em(1.5),
          color("#374151"),
          width.pct(100),
          maxWidth.none,
          background("#ffffff"),
          borderRadius.px(12),
          border.none,
          boxShadow("0 4px 6px -1px rgba(0, 0, 0, 0.08), 0 2px 4px -2px rgba(0, 0, 0, 0.05)"),
          boxSizing.borderBox,
          overflow.hidden,
          // Leading space = descendant (CssClass concatenates without inserting one).
          Selector(" *")(boxSizing.borderBox),
          Selector(" section > ul")(maxWidth.none, width.pct(100)),
        )

    // Header with gradient background and title/input.
    object Header
        extends CssClass(
          background("linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)"),
          padding("24px 20px 20px"),
          color("#ffffff"),
        )

    object Title
        extends CssClass(
          fontSize.px(22),
          fontWeight(700),
          color("#ffffff"),
          margin.zero,
          marginBottom.px(14),
          letterSpacing("-0.02em"),
        )

    // Input for adding new todos.
    object AddInput
        extends CssClass(
          width.pct(100),
          padding("11px 14px"),
          fontSize.px(14),
          borderRadius.px(8),
          border.none,
          S("outline", "none"),
          boxSizing.borderBox,
          background("#ffffff"),
          color("#1f2937"),
          boxShadow("0 1px 3px rgba(0, 0, 0, 0.12)"),
          transition("box-shadow 0.2s ease-in-out, transform 0.15s ease-in-out"),
          Selector(":focus")(
            boxShadow("0 4px 12px rgba(0, 0, 0, 0.18)"),
            S("transform", "translateY(-1px)"),
          ),
          Selector("::placeholder")(color("#9ca3af")),
        )

    // Main list area (applied on the <ul> itself).
    // maxWidth override lives on App via `section > ul` so it beats specular specificity.
    object ListArea
        extends CssClass(
          width.pct(100),
          maxWidth.none,
          background("#ffffff"),
          maxHeight.px(280),
          overflowY.auto,
          listStyle.none,
          margin.zero,
          padding.zero,
          Selector("::-webkit-scrollbar")(width.px(4)),
          Selector("::-webkit-scrollbar-thumb")(background("#d1d5db"), borderRadius.px(2)),
        )

    // Keyframe for completion animation (subtle scale + fade).
    object CompleteAnim
        extends CssClass(
          S("animation", "todo-complete 0.35s cubic-bezier(0.34, 1.56, 0.64, 1) forwards"),
          Selector("@keyframes todo-complete")(
            S("0%")(S("transform", "scale(1)"), color("#374151")),
            S("40%")(S("transform", "scale(0.97)"), color("#6366f1")),
            S("100%")(S("transform", "scale(1)"), color("#d1d5db")),
          ),
        )

    // Keyframe for item entry (slide up + fade in).
    object EnterAnim
        extends CssClass(
          S("animation", "todo-enter 0.25s cubic-bezier(0.34, 1.56, 0.64, 1) forwards"),
          Selector("@keyframes todo-enter")(
            S("0%")(S("transform", "translateY(-8px) scale(0.97)"), opacity(0)),
            S("100%")(S("transform", "translateY(0) scale(1)"), opacity(1)),
          ),
        )

    // Individual todo item row with fade-in animation.
    object ItemRow
        extends CssClass(
          display.flex,
          alignItems.center,
          width.pct(100),
          padding("12px 20px"),
          borderBottom("1px solid #f3f4f6"),
          transition("background-color 0.2s ease-in-out, transform 0.15s ease-in-out"),
          Selector(":hover")(background("#fafbff")),
        )

    // Checkbox for toggling completion with bounce on check.
    object ToggleCheckbox
        extends CssClass(
          width.px(20),
          height.px(20),
          marginRight.px(12),
          cursor.pointer,
          S("accent-color", "#6366f1"),
          flexShrink(0),
          borderRadius.px(4),
          transition("transform 0.15s cubic-bezier(0.34, 1.56, 0.64, 1)"),
          Selector(":checked")(S("transform", "scale(1.15)")),
        )

    // Todo text label.
    object ItemText
        extends CssClass(
          S("flex", "1"),
          cursor.default,
          userSelect.none,
          transition("color 0.2s ease-in-out, text-decoration-color 0.2s ease-in-out"),
          fontWeight(400),
        )

    // Completed item styling with smooth fade and subtle highlight.
    object DoneText
        extends CssClass(
          S("text-decoration", "line-through"),
          color("#d1d5db"),
          transition("color 0.3s ease-in-out, text-decoration-color 0.3s ease-in-out, opacity 0.25s ease-in-out"),
          opacity(0.85),
        )

    // Row background for completed items (soft violet tint, full row width).
    object DoneRow
        extends CssClass(
          background("#f5f3ff"),
          transition("background-color 0.3s ease-in-out"),
        )

    // Delete button class name (used by ItemRowHover selector).
    object DeleteBtnClass extends CssClass()

    // Delete button styling (shown on hover).
    object DeleteBtn
        extends CssClass(
          marginLeft.px(8),
          padding("5px 7px"),
          fontSize.px(14),
          borderRadius.px(6),
          border.none,
          S("background", "transparent"),
          color("#ef4444"),
          cursor.pointer,
          opacity(0.0),
          transition("opacity 0.15s ease-in-out, background-color 0.15s ease-in-out, transform 0.15s ease-in-out"),
          Selector(":hover")(background("#fef2f2")),
        )

    // Row hover reveals delete button.
    object ItemRowHover
        extends CssClass(
          Selector(":hover .todo-delete-btn")(opacity(1.0))
        )

    // Inline editing input.
    object EditInput
        extends CssClass(
          S("flex", "1"),
          marginLeft.px(12),
          padding("7px 10px"),
          fontSize.px(14),
          borderRadius.px(6),
          border("1px solid #6366f1"),
          boxShadow("0 0 0 3px rgba(99, 102, 241, 0.1)"),
          S("outline", "none"),
        )

    // Footer with stats and filters.
    object Footer
        extends CssClass(
          display.flex,
          alignItems.center,
          justifyContent.spaceBetween,
          width.pct(100),
          padding("10px 20px"),
          background("#fafbff"),
          borderTop("1px solid #f3f4f6"),
          fontSize.px(12),
          color("#6b7280"),
        )

    // Left side of footer: count + clear button.
    object FooterLeft
        extends CssClass(
          display.flex,
          alignItems.center,
          S("gap", "14px"),
        )

    // Clear completed button.
    object ClearBtn
        extends CssClass(
          S("background", "transparent"),
          border.none,
          color("#6b7280"),
          cursor.pointer,
          fontSize.px(12),
          padding.zero,
          textDecoration.none,
          transition("color 0.15s ease-in-out"),
          Selector(":hover")(color("#ef4444")),
        )

    // Filter buttons group.
    object Filters
        extends CssClass(
          display.flex,
          S("gap", "2px"),
          background("#f3f4f6"),
          padding.px(2),
          borderRadius.px(8),
        )

    // Individual filter button (pill style).
    object FilterBtn
        extends CssClass(
          padding("4px 12px"),
          borderRadius.px(6),
          border.none,
          S("background", "transparent"),
          color("#6b7280"),
          cursor.pointer,
          fontSize.px(12),
          fontWeight(500),
          transition("all 0.2s ease-in-out"),
          Selector(":hover")(color("#374151")),
        )

    // Active filter button styling.
    object FilterBtnActive
        extends CssClass(
          background("#ffffff"),
          color("#6366f1"),
          fontWeight(600),
          boxShadow("0 1px 3px rgba(0, 0, 0, 0.1)"),
        )

    // Toggle all checkbox label area.
    object ToggleAllArea
        extends CssClass(
          display.flex,
          alignItems.center,
          width.pct(100),
          padding("8px 20px"),
          borderBottom("1px solid #f3f4f6"),
          background("#fafbff"),
          fontSize.px(12),
          color("#6b7280"),
        )

    object ToggleAllCheckbox
        extends CssClass(
          marginRight.px(6),
          cursor.pointer,
          S("accent-color", "#6366f1"),
        )

    // Empty state message.
    object EmptyState
        extends CssClass(
          padding("40px 24px"),
          textAlign.center,
          color("#9ca3af"),
          fontSize.px(14),
          lineHeight.em(1.6),
        )
  end styles
  // specular:end

  // specular:begin demo-components
  // ---- Components ----

  private val ENTER  = 13
  private val ESCAPE = 27

  /** Root app component — subscribes to the full model via conduit.component. */
  val App = todoConduit.component { model =>
    console.log(s"[TodoDemo] App render: ${model.items.size} items")
    E.div(
      styles.App,
      Header(model),
      ListArea(model),
      FooterBar(model),
    )
  }

  /** Header with title and add-todo input. */
  object Header extends Component[TodoModel]:
    def render(model: TodoModel): VNode =
      E.header(
        styles.Header,
        S.margin.zero,
        E.div(
          S.display.flex,
          S.alignItems.center,
          S.justifyContent.spaceBetween,
          S.width.pct(100),
          E.h2(styles.Title, "✨ Todos"),
          when(model.items.nonEmpty) {
            val activeCount = model.items.count(!_.done)
            val allDone     = activeCount == 0
            E.span(
              S.fontSize.px(12),
              S.fontWeight(500),
              S.padding("3px 8px"),
              S.borderRadius.px(99),
              S.background(if allDone then "rgba(74, 222, 128, 0.3)" else "rgba(255,255,255,0.2)"),
              S.transition("background-color 0.3s ease-in-out"),
              if allDone then "🎉 All done!" else s"$activeCount active",
            )
          },
        ),
        E.input(
          styles.AddInput,
          A.placeholder("What needs to be done?"),
          A.onKeyDown { e =>
            console.log(s"[TodoDemo] keyDown: keyCode=${e.keyCode}, key=${e.key}")
            if e.keyCode == ENTER then
              val input = e.target.asInstanceOf[HTMLInputElement]
              console.log(s"[TodoDemo] Enter pressed, value='${input.value}'")
              if input.value.nonEmpty then
                todoConduit.unsafe(TodoActions.Add(input.value.trim))
                todoConduit.unsafe.run(true)
                input.value = ""
          },
        ),
      )
  end Header

  /** List area with toggle-all checkbox and items. */
  object ListArea extends Component[TodoModel]:
    def render(model: TodoModel): VNode =
      val filtered = model.items.filter(matchFilter(_, model.filter))
      E.section(
        S.margin.zero,
        if filtered.isEmpty then
          E.div(
            styles.EmptyState,
            E.span(S.fontSize.px(28), S.display.block, S.marginBottom.px(8), "📝"),
            E.div(S.fontWeight(500), S.color("#6b7280"), S.marginBottom.px(4), "No todos yet"),
            E.div(S.fontSize.px(13), "Add one above to get started!"),
          )
        else
          fragment(
            ToggleAllRow(model),
            E.ul(
              styles.ListArea,
              filtered.map(item => Item(item).withKey(item.key)).toList,
            ),
          ),
      )
    end render

    private def matchFilter(todo: Todo, filter: Filter): Boolean =
      (filter, todo.done) match
        case (_, true) if filter == Filter.Active     => false
        case (_, false) if filter == Filter.Completed => false
        case _                                        => true
  end ListArea

  /** Toggle-all checkbox row. */
  object ToggleAllRow extends Component[TodoModel]:
    def render(model: TodoModel): VNode =
      val allDone = model.items.nonEmpty && model.items.forall(_.done)
      E.div(
        styles.ToggleAllArea,
        E.input(
          styles.ToggleAllCheckbox,
          A.`type`("checkbox"),
          A.checked(allDone),
          A.onChange { _ =>
            if model.items.exists(!_.done) then
              todoConduit.unsafe(TodoActions.SetAllDone(true))
              todoConduit.unsafe.run(true)
            else
              todoConduit.unsafe(TodoActions.SetAllDone(false))
              todoConduit.unsafe.run(true)
          },
        ),
        "Mark all complete",
      )
    end render
  end ToggleAllRow

  /** Individual todo item — renders either viewer or editor based on editing state. */
  object Item extends Component[Todo]:
    def render(todo: Todo): VNode =
      E.li(
        styles.ItemRow,
        styles.ItemRowHover,
        styles.DoneRow.when(todo.done),
        styles.EnterAnim,
        if todo.editing then Editor(todo) else Viewer(todo),
      )

  /** View mode: checkbox + text label + delete button. */
  object Viewer extends Component[Todo]:
    def render(todo: Todo): VNode =
      E.div(
        S.display.flex,
        S.alignItems.center,
        S.width.pct(100),
        E.input(
          styles.ToggleCheckbox,
          A.`type`("checkbox"),
          A.checked(todo.done),
          A.onChange { _ =>
            todoConduit.unsafe(TodoActions.Toggle(todo.key))
            todoConduit.unsafe.run(true)
          },
        ),
        E.span(
          styles.ItemText,
          styles.DoneText.when(todo.done),
          styles.CompleteAnim.when(todo.done),
          todo.text,
          A.onDoubleClick { _ =>
            todoConduit.unsafe(TodoActions.StartEditing(todo.key))
            todoConduit.unsafe.run(true)
          },
        ),
        E.button(
          styles.DeleteBtn,
          A.`class`("todo-delete-btn"), // class name used by ItemRowHover selector
          A.onClick { _ =>
            todoConduit.unsafe(TodoActions.Delete(todo.key))
            todoConduit.unsafe.run(true)
          },
          "✕",
        ),
      )
  end Viewer

  /** Edit mode: inline text input with Enter/Escape handling. */
  object Editor extends StatefulComponent[Todo, String]:
    override def initialState(todo: Todo): String = todo.text

    def render(props: Todo, state: String, instance: Instance): VNode =
      E.div(
        S.display.flex,
        S.alignItems.center,
        // Spacer to align with checkbox position.
        E.span(S.width.px(18), S.marginRight.px(10)),
        E.input(
          styles.EditInput,
          A.value(state),
          A.onKeyDown { e =>
            e.keyCode match
              case ENTER =>
                todoConduit.unsafe(TodoActions.FinishEditing(props.key, state))
                todoConduit.unsafe.run(true)
              case ESCAPE =>
                todoConduit.unsafe(TodoActions.CancelEditing(props.key))
                todoConduit.unsafe.run(true)
              case _ => ()
          },
          A.onKeyUp(_ => instance.setState(instance.base.asInstanceOf[HTMLInputElement].value)),
          A.onBlur { _ =>
            todoConduit.unsafe(TodoActions.FinishEditing(props.key, state))
            todoConduit.unsafe.run(true)
          },
        ).withRef(x => setTimeout(1)(x.asInstanceOf[HTMLInputElement].focus())),
      )
  end Editor

  /** Footer bar with item count, clear button, and filter buttons. */
  object FooterBar extends Component[TodoModel]:
    def render(model: TodoModel): VNode =
      val activeCount  = model.items.count(!_.done)
      val hasCompleted = model.items.exists(_.done)

      E.footer(
        styles.Footer,
        S.margin.zero,
        // Left side: count + clear completed (with subtle pop on change).
        E.div(
          styles.FooterLeft,
          E.span(
            S.display.inlineBlock,
            S.transition("transform 0.15s cubic-bezier(0.34, 1.56, 0.64, 1)"),
            s"$activeCount item${if activeCount != 1 then "s" else ""} left",
          ),
          when(hasCompleted) {
            E.button(
              styles.ClearBtn,
              A.onClick { _ =>
                todoConduit.unsafe(TodoActions.ClearCompleted)
                todoConduit.unsafe.run(true)
              },
              "Clear completed",
            )
          },
        ),
        // Right side: filter buttons.
        E.div(styles.Filters, FilterButton(Filter.All), FilterButton(Filter.Active), FilterButton(Filter.Completed)),
      )
    end render
  end FooterBar

  /** Filter button component — uses a lens to subscribe only to the filter field. */
  def FilterButton(target: Filter): VNode =
    todoConduit
      .component(_.filter) { current =>
        val active = current == target
        E.button(
          styles.FilterBtn,
          styles.FilterBtnActive.when(active),
          A.onClick { _ =>
            todoConduit.unsafe(TodoActions.SetFilter(target))
            todoConduit.unsafe.run(true)
          },
          target.getClass.getSimpleName.init, // "All", "Active", "Completed"
        )
      }
      .apply(())

  // specular:end

  /** Mounter for the docs site. */
  val mounter = Mounter.sync { el => preactile.preact.render(App(()), el) }

end TodoDemo
