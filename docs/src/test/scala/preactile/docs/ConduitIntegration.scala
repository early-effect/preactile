package preactile.docs

import specular.*
import specular.ziotest.DocSpecSuite

object ConduitIntegration extends DocSpecSuite:
  def doc = page("Conduit Integration")(
    md"""
# Conduit Integration

Preactile integrates with [conduit](https://github.com/early-effect/conduit) to provide
a clean, effect-native state management pattern. Components subscribe to slices of your
model and automatically re-render when relevant data changes.

## Installation

Add the conduit module alongside preactile (`%%` is enough on a Scala.js project):

```scala
libraryDependencies ++= Seq(
  "rocks.earlyeffect" %% "preactile"         % "<version>",
  "rocks.earlyeffect" %% "preactile-conduit" % "<version>",
)
```

The index page shows the current published version.

## Basic usage

Define a model with `Optics`, create a handler, then build a conduit with `Conduit.make`:

```scala
import conduit.*

case class Counter(count: Int) derives Optics

sealed trait CountAction extends Action
object CountActions:
  case object Increment extends CountAction
  case object Decrement extends CountAction

val counterHandler = handle[Counter, CountAction]:
  case CountActions.Increment => update(_.copy(count = _.count + 1))
  case CountActions.Decrement => update(_.copy(count = _.count - 1))

val counterConduit = Conduit.make(Counter(0))(counterHandler)
```

`Conduit.make` is the synchronous constructor. There is no `Conduit.live`.

## Creating conduit components

Use the `component` extension on your conduit. The simplest form takes a render function
over the whole model:

```scala
import preactile.*

val CounterView = counterConduit.component { model =>
  E.div(
    E.span("Count: "),
    E.strong(model.count.toString),
  )
}
```

This component subscribes to the entire model and re-renders on any change.

## Using lenses for focused subscriptions

For larger models, pass a path so the component only re-renders when that slice changes:

```scala
import conduit.*
import preactile.*

case class User(name: String, age: Int) derives Optics
case class AppState(user: User, theme: String) derives Optics

val userConduit = Conduit.make(AppState(User("Alice", 30), "light"))(handler)

val UserNameView = userConduit.component(_.user.name) { name =>
  E.h2(s"Hello, $$name!")
}
```

`UserNameView` only re-renders when the user's name changes, not on unrelated state updates.

## Components with props and lenses

Combine external props with a lens-subscribed state:

```scala
import preactile.*

val UserProfile = userConduit.component(_.user) { (showAge: Boolean, user: User) =>
  E.div(
    E.h2(user.name),
    when(showAge)(E.p(s"Age: $${user.age}")),
  )
}

UserProfile(true)   // name and age
UserProfile(false)  // name only
```

## Sending actions from components

Event handlers enqueue with `conduit.unsafe(action)` (not `unsafe.send`). The conduit
does not apply anything until you drain the queue. In a Preactile event handler that
is `conduit.unsafe.run(true)`: drain currently queued actions (and any follow-ups)
and return.

```scala
import preactile.*

val CounterControls = counterConduit.component { model =>
  E.div(
    E.button(
      A.onClick { _ =>
        counterConduit.unsafe(CountActions.Decrement)
        counterConduit.unsafe.run(true)
      },
      "-",
    ),
    E.strong(model.count.toString),
    E.button(
      A.onClick { _ =>
        counterConduit.unsafe(CountActions.Increment)
        counterConduit.unsafe.run(true)
      },
      "+",
    ),
  )
}
```

A long-running app (the example todo) can instead call `conduit.run(false)` once from
`@main` and then only enqueue with `unsafe(action)`.

## Lifecycle

`ConduitComponent` manages the subscription itself: it zooms and subscribes in
`componentWillMount`, and unsubscribes in `componentWillUnmount`. You do not need to
override those hooks for the subscription to work.

## Live demo: Todo app with conduit

A complete todo application built with preactile and conduit, rendered live in your
browser. Add items, toggle completion, filter by status, double-click to edit inline,
or clear completed todos.
""",
    exampleDom("conduit-todo").fromSource(
      "docs/client/src/main/scala/preactile/docs/TodoDemo.scala",
      "demo-components",
    ),
    md"""
### How it works

The demo above uses several conduit patterns.

**Model and handler** — A `TodoModel` with items and a filter derives `Optics`. The
handler processes actions by pattern matching. The conduit is `Conduit.make`:

```scala
case class Todo(key: String, text: String, done: Boolean) derives Optics

sealed trait Filter extends Product with Serializable
object Filter:
  case object All       extends Filter
  case object Active    extends Filter
  case object Completed extends Filter

case class TodoModel(items: List[Todo], filter: Filter) derives Optics

sealed trait TodoAction extends Action
object TodoActions:
  final case class Add(text: String)    extends TodoAction
  final case class Toggle(key: String)  extends TodoAction
  final case class Delete(key: String)  extends TodoAction
  final case class SetFilter(f: Filter) extends TodoAction
  case object ClearCompleted            extends TodoAction

val todoHandler = handle[TodoModel, TodoAction]:
  case TodoActions.Add(text) => update(m => m.copy(items = m.items :+ Todo(text)))
  case TodoActions.Toggle(key) =>
    update(m => m.copy(items = m.items.map(t => if t.key == key then t.copy(done = !t.done) else t)))
  // ... other cases

val todoConduit = Conduit.make(TodoModel(Nil, Filter.All))(todoHandler)
```
""",
    md"""
**Conduit components** — The root `App` is `todoConduit.component { model => … }`
(full-model subscription). Filter buttons use a path (`_.filter`) so they only
re-render when the filter changes:

```scala
val App = todoConduit.component { model =>
  E.div(styles.App, Header(model), ListArea(model), FooterBar(model))
}

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
        target.toString,
      )
    }
    .apply(())
```

**Sending actions** — Event handlers enqueue, then drain:

```scala
A.onChange { _ =>
  todoConduit.unsafe(TodoActions.Toggle(todo.key))
  todoConduit.unsafe.run(true)
}

if e.keyCode == ENTER && input.value.nonEmpty then
  todoConduit.unsafe(TodoActions.Add(input.value.trim))
  todoConduit.unsafe.run(true)
  input.value = ""
```

**Styling** — All of the demo styling is `CssClass` plus `S.*`. No external stylesheet:

```scala
object styles:
  import S.*

  object App extends CssClass(
    fontFamily("system-ui, -apple-system, sans-serif"),
    borderRadius.px(8),
    border("1px solid #e5e7eb"),
  )

  object DoneText extends CssClass(
    S("text-decoration", "line-through"),
    color("#9ca3af"),
  )
```
""",
    md"""
See the [Examples](/Examples) page for the other live demos. Live host exports are
on [Embedding in React](/EmbeddingInReact) and [Embedding in Preact](/EmbeddingInPreact).
""",
  )

end ConduitIntegration
