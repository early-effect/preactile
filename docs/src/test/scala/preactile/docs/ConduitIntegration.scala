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

       Add the conduit module alongside preactile:

       ```scala
       libraryDependencies ++= Seq(
         "rocks.earlyeffect" %% "preactile"          % "@VERSION@",
         "rocks.earlyeffect" %% "preactile-conduit"  % "@VERSION@",
       )
       ```

       ## Basic usage

       Define a model with `Optics`, create a handler, then wire it to components:

       ```scala mdoc:compile-only
       import conduit.*

       case class Counter(count: Int) derives Optics

       sealed trait CountAction extends Action
       object CountActions:
         case object Increment extends CountAction
         case object Decrement extends CountAction

       val counterHandler = handle[Counter, CountAction]:
         case CountActions.Increment => update(_.copy(count = _.count + 1))
         case CountActions.Decrement => update(_.copy(count = _.count - 1))
       ```

       Create a conduit from the handler and initial model:

       ```scala mdoc:compile-only
       import zio.Runtime

       val counterConduit = Conduit.live(Counter(0), counterHandler)
         .provideSomeLayer[ZIO[Any, Nothing, ?]](Runtime.default.runtime)
       ```

       ## Creating conduit components

       Use the `component` extension method on your conduit. The simplest form takes a render function:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       val CounterView = counterConduit.component { model =>
         div(
           span("Count: "),
           strong(model.count.toString),
         )
       }
       ```

       This component subscribes to the entire model and re-renders on any change.

       ## Using lenses for focused subscriptions

       For larger models, use a lens to subscribe only to the slice your component needs:

       ```scala mdoc:compile-only
       import conduit.*

       case class User(name: String, age: Int) derives Optics
       case class AppState(user: User, theme: String) derives Optics

       val userConduit = Conduit.live(AppState(User("Alice", 30)), handler)

       val UserNameView = userConduit.component(_.user.name) { name =>
         E.h2(s"Hello, $$name!")
       }
       ```

       `UserNameView` only re-renders when the user's name changes, not on unrelated state updates.

       ## Components with props and lenses

       Combine external props with a lens-subscribed state:

       ```scala mdoc:compile-only
       import preactile.*

       val UserProfile = userConduit.component(_.user) { (showAge: Boolean, user: User) =>
         E.div(
           E.h2(user.name),
           when(showAge)(E.p(s"Age: $${user.age}")),
         )
       }
       ```

       Use it with props like any other component:

       ```scala mdoc:compile-only
       UserProfile(true)   // shows name and age
       UserProfile(false)  // shows name only
       ```

       ## Sending actions from components

       Components trigger state changes by sending actions to the conduit. The pattern is simple:
       call `conduit.unsafe.send(action)` in event handlers:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       val CounterControls = counterConduit.component { model =>
         div(
           button(
             onclick := (_ => counterConduit.unsafe.send(CountActions.Decrement)),
             "-",
           ),
           span(" "),
           strong(model.count.toString),
           span(" "),
           button(
             onclick := (_ => counterConduit.unsafe.send(CountActions.Increment)),
             "+",
           ),
         )
       }
       ```

       ## Lifecycle hooks

       ConduitComponent supports the same lifecycle hooks as StatefulComponent:

       ```scala mdoc:compile-only
       import preactile.*
       import preactile.dsl._

       abstract class MyConduitComponent[Props, Model <: Product: Optics, Event, State](
         conduit: Conduit[Model, Event],
       ) extends ConduitComponent[Props, Model, Event, State](conduit):

         override def didMount(instance: Instance): Unit =
           println("Mounted and subscribed to conduit")

         override def willUnMount(instance: Instance): Unit =
           println("Unmounted; subscription cleaned up automatically")
       ```

       The conduit subscription is managed automatically: it's created in `componentWillMount`
       and unsubscribed in `componentWillUnmount`.

       ## Live demo: Todo app with conduit

       Here's a complete todo application built with preactile and conduit, rendered live in your browser.
       Try it out — add items, toggle completion, filter by status, double-click to edit inline, or clear completed todos.
    """,
    exampleDom("conduit-todo").fromSource(
      "docs/client/src/main/scala/preactile/docs/TodoDemo.scala",
      "demo-components",
    ),
    md"""
       ### How it works

       The demo above uses several conduit patterns:

       **Model and handler** — A `TodoModel` with items and a filter derives `Optics`, and the handler processes actions via pattern matching:

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
         final case class Add(text: String)      extends TodoAction
         final case class Toggle(key: String)    extends TodoAction
         final case class Delete(key: String)    extends TodoAction
         final case class SetFilter(f: Filter)   extends TodoAction
         case object ClearCompleted              extends TodoAction

       val todoHandler = handle[TodoModel, TodoAction]:
         case TodoActions.Add(text) => update(m => m.copy(items = m.items :+ Todo(text)))
         case TodoActions.Toggle(key) =>
           update(m => m.copy(items = m.items.map(t => if t.key == key then t.copy(done = !t.done) else t)))
         // ... other cases

       val todoConduit = Conduit.make(TodoModel(Nil, Filter.All))(todoHandler)
       ```
    """,
    md"""
       **Conduit components** — Components subscribe to the model using `todoConduit.component { model => ... }`. The root `App` subscribes to the full model, while individual filter buttons use lenses (`_.filter`) to subscribe only to what they need:

       ```scala
       // Full model subscription (re-renders on any change)
       object App extends Component[Unit]:
         def render(props: Unit): VNode = todoConduit.component { model =>
           E.div(styles.App, Header(model), ListArea(model), FooterBar(model))
         }

       // Lens-based subscription (only re-renders when filter changes)
       object FilterButton extends Component[Filter]:
         def render(target: Filter): VNode = todoConduit.component(_.filter) { current =>
           val active = current == target
           E.button(styles.FilterBtn, styles.FilterBtnActive.when(active), ...)
         }
       ```

       **Sending actions** — Event handlers send actions to the conduit using `todoConduit.unsafe(action)`:

       ```scala
       // Toggle completion on checkbox change
       A.onChange(_ => todoConduit.unsafe(TodoActions.Toggle(todo.key)))

       // Add a new todo on Enter key
       if e.keyCode == ENTER && input.value.nonEmpty then
         todoConduit.unsafe(TodoActions.Add(input.value.trim))
         input.value = ""
       ```

       **Styling with Preactile CSS DSL** — All styling uses preactile's `CssClass` and `S.*` utilities, no external stylesheets:

       ```scala
       object styles:
         import S.*

         object App extends CssClass(
           fontFamily("system-ui, -apple-system, sans-serif"),
           borderRadius.px(8),
           border("1px solid #e5e7eb"),
         )

         object ItemRow extends CssClass(
           display.flex,
           alignItems.center,
           padding("10px 16px"),
           borderBottom("1px solid #f3f4f6"),
           Selector(":hover")(background("#fafafa")),
         )

         object DoneText extends CssClass(
           S("text-decoration", "line-through"),
           color("#9ca3af"),
         )
       ```
    """,
    md"""
       See the [Examples](/Examples) page for more interactive demos.
       """,
  )

end ConduitIntegration
