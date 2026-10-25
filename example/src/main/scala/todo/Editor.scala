package todo

import scala.scalajs.js.timers

import org.scalajs.dom.HTMLInputElement
import todo.model.*
import todo.model.TodoList.actions.CancelEditing
import todo.model.TodoList.actions.FinishEditing

import preactile.*
import preactile.dsl.css.CssClass

object Editor extends StatefulComponent[Todo, String] with InstanceDataSelector with ClassSelector:

  override def extractAttributeValue(instance: Instance): String = instance.props.key

  override def initialState(t: Todo): String = t.description

  object css:
    import S.*

    object Editing
        extends CssClass(
          font.inherit,
          border("1px solid #999"),
          position.relative,
          display.block,
          width.px(506),
          margin("0 0 0 43px"),
          fontSize.px(24),
          padding("12px 16px"),
          boxSizing.borderBox,
          boxShadow("inset 0 -1px 5px 0 rgba(0, 0, 0, 0.2)"),
        )
  end css

  override def render(props: Todo, state: String, instance: Instance): VNode =
    def finishEditing(): Unit = Todos(FinishEditing(props, state))
    def cancelEditing(): Unit = Todos(CancelEditing(props))
    E.input(
      css.Editing,
      A.value(state),
      A.onKeyDown { k =>
        k.keyCode match
          case ESCAPE => cancelEditing()
          case ENTER  => finishEditing()
          case _      => ()
      },
      A.onKeyUp(_ => instance.setState(instance.base.asInstanceOf[HTMLInputElement].value)),
      A.onBlur(_ => finishEditing()),
    ).withRef(x => timers.setTimeout(1)(x.asInstanceOf[HTMLInputElement].focus()))
end Editor
