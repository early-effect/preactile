package todo.model

import zio.*

import conduit.*

val echo = handle[TodoList, Nothing]:
  case a => m => Console.printLine(s"$a: $m").orDie.as(ActionResult.clean(m))

val TodosConduit = Conduit.make(
  TodoList(Seq.empty, Filter.All, true)
)(
  TodoList.handler ++ echo
)

val Todos = TodosConduit.unsafe
