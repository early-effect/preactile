package todo.model

import conduit.*

case class TodoList(todos: Seq[Todo], filter: Filter, online: Boolean) derives Optics:
  lazy val countIncomplete: Int    = todos.count(!_.complete)
  def delete(todo: Todo): TodoList = copy(todos = todos.filter(_.key != todo.key))
  def add(todo: Todo): TodoList    = copy(todos = todos :+ todo)
  def update(todo: Todo): TodoList = copy(todos = todos.updated(todos.indexWhere(_.key == todo.key), todo))

  def finishEditing(todo: Todo, description: String): TodoList =
    update(todo.copy(description = description, editing = false))

  def cancelEditing(todo: Todo): TodoList = update(todo.copy(editing = false))
  def complete(todo: Todo): TodoList      = update(todo.copy(complete = true))
  def clearCompleted: TodoList            = copy(todos = todos.filterNot(_.complete))
  def setAll(complete: Boolean): TodoList = copy(todos = todos.map(x => x.copy(complete = complete)))

  def filtered: Seq[Todo] =
    import Filter.*
    filter match
      case All       => todos
      case Completed => todos.filter(_.complete)
      case Active    => todos.filterNot(_.complete)
end TodoList

sealed trait Filter

object Filter:
  case object All       extends Filter
  case object Completed extends Filter
  case object Active    extends Filter

object TodoList:
  import todo.model.TodoList.actions.*

  val handler = handle[TodoList, Nothing]:
    case GoOnline                         => update(_.copy(online = true))
    case GoOffline                        => update(_.copy(online = false))
    case Add(todo)                        => update(_.add(todo))
    case Delete(todo)                     => update(_.delete(todo))
    case Update(todo)                     => update(_.update(todo))
    case ApplyFilter(f)                   => update(_.copy(filter = f))
    case ClearCompleted                   => update(_.clearCompleted)
    case SetAll(complete)                 => update(_.setAll(complete))
    case CancelEditing(todo)              => update(_.cancelEditing(todo))
    case FinishEditing(todo, description) => update(_.finishEditing(todo, description))

  object actions:
    case object GoOnline                                            extends Action
    case object GoOffline                                           extends Action
    final case class Add(todo: Todo)                                extends Action
    final case class Delete(todo: Todo)                             extends Action
    final case class Update(todo: Todo)                             extends Action
    final case class ApplyFilter(filter: Filter)                    extends Action
    case object ClearCompleted                                      extends Action
    final case class SetAll(boolean: Boolean)                       extends Action
    final case class FinishEditing(todo: Todo, description: String) extends Action
    final case class CancelEditing(todo: Todo)                      extends Action
  end actions
end TodoList
