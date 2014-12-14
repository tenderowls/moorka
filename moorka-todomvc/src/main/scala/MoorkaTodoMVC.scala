
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.ui.components.html._
import com.tenderowls.moorka.ui.components.base._

object MoorkaTodoMVC extends Application {
  def start() = {
    // Inject Data
    val todos = Collection.fromSeq(
      0 to 20 map(x => Todo(s"$x todo", Active))
    )
    // The values represents display state of this component.
    // They are depends on domain data
    val numCompleted = todos.foldLeft(0) {
      case (num, Todo(_, Completed)) => num + 1
      case (num, _) => num
    }
    val numActive = todos.foldLeft(0) {
      case (num, Todo(_, Active)) => num + 1
      case (num, _) => num
    }
    val nowEditing = Var(false)
    val filter = Var[Filter](All)
    // Internal component. It has own state but depends on
    // display state of main component
    case class TodoComponent(state: RxState[Todo]) extends Component[Todo] {
      def start() = {
        val fieldText = Var("")
        state.observe(fieldText() = state().txt)
        li(
          useClassName("completed") := state.map(_.status == Completed),
          useClassName("editing") := state.map(_.status == Editing),
          `hide` := (state zip filter) map {
            case (_, All) => false
            case (x, Completed) => x.status != Completed
            case (x, Active) => x.status == Completed
          },
          div(`className` := "view",
            `double-click` listen {
              val todo = state()
              if (todo.status == Active && !nowEditing()) {
                todos.update(todo, todo.copy(status = Editing))
                nowEditing() = true
              }
            },
            input(
              `className` := "toggle",
              `type` := "checkbox",
              `style` := "cursor: pointer",
              `click` listen {
                val todo = state()
                val newStatus = todo.status match {
                  case Active => Completed
                  case Completed => Active
                  case Editing => Active
                }
                todos.update(todo, todo.copy(status = newStatus))
              },
              `checked` := state.map(_.status == Completed)
            ),
            label(state.map(_.txt)),
            button(
              `className` := "destroy",
              `style` := "cursor: pointer",
              `click` listen {
                todos -= state()
              }
            )
          ),
          form(
            `submit` listen {
              val todo = state()
              todos.update(todo, Todo(txt = fieldText(), status = Active))
              nowEditing() = false
            },
            input(`className` := "edit", `value` =:= fieldText)
          )
        )
      }
    }
    // Tree of component
    div(
      section(`className` := "todoapp",
        header(`className` := "header",
          h1("todos"),
          // Sometimes we need to have isolated display state
          // for part of the tree. Using new [[Block]] { ... }
          // In fact we able to move this code to Component
          new Block {
            def start() = {
              val inputText = Var("")
              form(
                input(
                  `className` := "new-todo",
                  `placeholder` := "What needs to be done?",
                  `autofocus` := true,
                  `value` =:= inputText
                ),
                `submit` listen {
                  val s = inputText().trim()
                  if (s != "") {
                    todos += Todo(s, Active)
                    inputText() = ""
                  }
                }
              )
            }
          }
        ),
        section(`className` := "main",
          input(
            `className` := "toggle-all",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `checked` := Bind { todos.length > 0 && numCompleted() == todos.length },
            `click` subscribe { event =>
              `checked` from event.target onSuccess {
                case true =>
                  todos.updateAll(_.copy(status = Completed))
                case false =>
                  todos.updateAll(_.copy(status = Active))
              }
            }
          ),
          label(`for`:= "toggle-all", "Mark all as complete"),
          ul(`className` := "todo-list",
            Repeat[Todo](
              dataProvider = todos,
              (x: RxState[Todo]) => TodoComponent(x)
            )
          ),
          footer(`className` := "footer",
            span(`className` := "todo-count",
              strong( numActive.map(_.toString)),
              span(" item left")
            ),
            ul(`className` := "filters",
              Seq(All, Active, Completed).map { x =>
                li(
                  a(`href`:="#",
                    useClassName("selected") := Bind { filter() == x },
                    `click` listen {
                      filter() = x
                    },
                    x match {
                      case All => "All"
                      case Active => "Active"
                      case Completed => "Completed"
                    }
                  )
                )
              }
            ),
            button(
              `className` := "clear-completed",
              `show` := Bind { numCompleted() > 0 },
              `click` listen {
                todos.remove(_.status != Completed)
              },
              Bind {
                val doneString = numCompleted()
                s"Clear completed ($doneString)"
              }
            )
          )
        )
      ),
      footer(`className` := "info",
        p("Double-click to edit a todo")
      )
    )
  }
}