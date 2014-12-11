
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.ui.components.html._
import com.tenderowls.moorka.ui.components.base._

object MoorkaTodoMVC extends Application {

  case class Task(txt: Var[String], status: Var[Status])

  sealed trait Status

  sealed trait Filter

  case object Active extends Status with Filter

  case object Completed extends Status with Filter

  case object Editing extends Status

  case object All extends Filter

  val nowEditing = Var(false)

  val numCompleted = Var(0)

  val numActive = Var(0)

  val filter = Var[Filter](All)

  val filterFunction = Bind {
    filter() match {
      case All => x: Task => true
      case Completed => x: Task => x.status() == Completed
      case Active => x: Task => x.status() != Completed
    }
  }

  val todos = Collection.fromSeq((1 to 20).map {
    x => Task(new Var("Make something " + x.toString), new Var(Active))
  })

  def refreshCounters() = {
    numCompleted() = todos.count(_.status() == Completed)
    numActive() = todos.length - numCompleted()
  }

  todos.added.subscribe(_ => refreshCounters())
  todos.removed.subscribe(_ => refreshCounters())
  todos.inserted.subscribe(_ => refreshCounters())
  todos.updated.subscribe(_ => refreshCounters())

  refreshCounters()

  def start() = {

    val inputBox = input(
      `className` := "new-todo",
      `placeholder` := "What needs to be done?",
      `autofocus` := true
    )

    val todosView = Repeat[Task](dataProvider = todos.view, itemRenderer = { todo: Task =>
      li(
        mkClass("completed") := Bind { todo.status() == Completed },
        mkClass("editing") := Bind { todo.status() == Editing },
        div(`class` := "view",
          `double-click` listen {
            if (todo.status() == Active && !nowEditing()) {
              todo.status() = Editing
              nowEditing() = true
            }
          },
          input(
            `class` := "toggle",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `click` listen {
              todo.status() = todo.status() match {
                case Active => Completed
                case Completed => Active
                case Editing => Active
              }
              refreshCounters()
            },
            `checked` := todo.status.map {
              (x: Status) => x == Completed
            }
          ),
          label(todo.txt),
          button(
            `class` := "destroy",
            `style` := "cursor: pointer",
            `click` listen { _ =>
              todos -= todo
            }
          )
        ),
        form(
          `submit` listen {
            nowEditing() = false
            todo.status() = Active
          },
          input(`class` := "edit", `value` =:= todo.txt)
        )
      )
    })

    todosView.makeDataObservable(_.status)
    filterFunction observe { ff =>
      todosView.viewFilter(ff())
    }

    div(
      section(`className` := "todoapp",
        header(`className` := "header",
          h1("todos"),
          form(
            inputBox,
            `submit` listen {
              `value` from inputBox onSuccess { case x =>
                val s = x.trim()
                if (s != "") {
                  todos += Task(Var(s), Var(Active))
                  inputBox.ref.set(`value`.name, "")
                }
              }
            }
          )
        ),
        section(`className` := "main",
          input(
            `className` := "toggle-all",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `checked` := Bind { todos.length > 0 && numCompleted() == todos.length },
            `click` listen { event =>
              `checked` from event.target onSuccess {
                case true =>
                  todos.foreach(_.status() = Completed)
                  refreshCounters()
                case false =>
                  todos.foreach(_.status() = Active)
                  refreshCounters()
              }
            }
          ),
          label(`for`:= "toggle-all", "Mark all as complete"),
          ul(`className` := "todo-list", todosView),
          footer(`className` := "footer",
            span(`className` := "todo-count",
              strong( Bind { numActive().toString } ),
              span(" item left")
            ),
            ul(`className` := "filters",
              List(All, Active, Completed).map { x =>
                li(
                  a(`href`:="#",
                    mkClass("selected") := Bind { filter() == x },
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
              mkShow := Bind { numCompleted() > 0 },
              `click` listen {
                todos.remove(_.status() != Completed)
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