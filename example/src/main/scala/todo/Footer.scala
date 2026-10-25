package todo

import todo.model.*
import todo.model.TodoList.actions.ApplyFilter
import todo.model.TodoList.actions.ClearCompleted

import preactile.*
import preactile.dsl.css.CssClass

object Footer:
  def change(f: Filter) = Todos(ApplyFilter(f))

  object css:
    import S.*

    object Main
        extends CssClass(
          color("#777"),
          padding("10px 15px"),
          height.px(20),
          textAlign.center,
          borderTop("1px solid #e6e6e6"),
          Selector(
            ":before",
            content("''"),
            position.absolute,
            right.zero,
            bottom.zero,
            left.zero,
            right.zero,
            height.px(50),
            overflow.hidden,
            boxShadow(
              "0 1px 1px rgba(0, 0, 0, 0.2), 0 8px 0 -3px rgba(246, 246, 246, 0.51), 0 9px 1px -3px rgba(0, 0, 0, 0.2), 0 16px 0 -6px rgba(246, 246, 246, 0.51),0 17px 2px -6px rgba(0, 0, 0, 0.2)"
            ),
          ),
        )

    object TodoCount
        extends CssClass(
          float.left,
          textAlign.left,
          Selector(" strong", font.inherit, fontWeight(300)),
        )

    object Filters
        extends CssClass(
          margin.zero,
          padding.zero,
          listStyle.none,
          position.absolute,
          right.zero,
          left.zero,
        )

    object ClearCompleted
        extends CssClass(
          float.right,
          position.relative,
          lineHeight.px(20),
          textDecoration.none,
          cursor.pointer,
          margin.zero,
          padding.zero,
          border.zero,
          background.none,
          fontSize.pct(100),
          verticalAlign.baseline,
          font.inherit,
          color.inherit,
          appearance.none,
        )
  end css

  val component = TodosConduit.component: todoList =>
    val itemsLeftMessage = if todoList.countIncomplete > 1 then " items left" else " item left"

    E.footer(
      css.Main,
      E.span(css.TodoCount, E.strong(todoList.countIncomplete), E.span(itemsLeftMessage)),
      E.ul(
        css.Filters,
        FilterButton(todoList, Filter.All),
        FilterButton(todoList, Filter.Active),
        FilterButton(todoList, Filter.Completed),
      ),
      E.button(
        css.ClearCompleted,
        "Clear completed",
        A.onClick { _ =>
          Todos(ClearCompleted)
        },
      ),
    )

end Footer
