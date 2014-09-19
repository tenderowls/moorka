import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml._
import org.scalajs.dom

import scala.scalajs.js

object MoorkaTodoMVC extends js.JSApp with HTML {

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

  val todos = Collection[Task] //.fromSeq((1 to 200).map {
    //x => Task(new Var("Make something " + x.toString), new Var(Active))
  //})

  def refreshCounters() = {
    numCompleted() = todos.count(_.status() == Completed)
    numActive() = todos.length - numCompleted()
  }

  todos.added.subscribe(_ => refreshCounters())
  todos.removed.subscribe(_ => refreshCounters())
  todos.inserted.subscribe(_ => refreshCounters())
  todos.updated.subscribe(_ => refreshCounters())

  refreshCounters()

  def main(): Unit = {

    val inputBox = input(
      `id` := "new-todo",
      `placeholder` := "What needs to be done?",
      `autofocus` := true
    )

    val todosView = Repeat[Task](dataProvider = todos.view, itemRenderer = { todo: Task =>
      li(
        mkClass("completed") := Bind { todo.status() == Completed },
        mkClass("editing") := Bind { todo.status() == Editing },
        div(`class` := "view",
          `double-click` listen { _ =>
            if (todo.status() == Active && !nowEditing()) {
              todo.status() = Editing
              nowEditing() = true
            }
          },
          input(
            `class` := "toggle",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `click` listen { _ =>
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
            `click` listen { event => todos -= todo }
          )
        ),
        form(
          `submit` listen { event =>
            event.preventDefault()
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

    val component = {
      section(`id` := "todoapp",
        header(`id` := "header",
          h1("todos"),
          form(
            inputBox,
            `submit` listen { event =>
              event.preventDefault()
              val s = `value` extractFrom inputBox trim()
              if (s != "") {
                todos += Task(Var(s), Var(Active))
                inputBox.setProperty(`value`, "")
              }
              false
            }
          )
        ),
        section(`id` := "main",
          input(
            `id` := "toggle-all",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            // todo length bust be var
            `checked` := Bind { todos.length > 0 && numCompleted() == todos.length },
            `click` listen { event =>
              val newStatus = `checked` extractFrom event.target match {
                case true => Completed
                case false => Active
              }
              todos.foreach(_.status() = newStatus)
              refreshCounters()
            }
          ),
          label(`for`:= "toggle-all", "Mark all as complete"),
          ul(`id` := "todo-list", todosView),
          footer( `id` := "footer",
            span( `id` := "todo-count",
              strong( Bind { numActive().toString } ),
              span(" item left")
            ),
            ul(`id` := "filters",
              List(All, Active, Completed).map { x =>
                li(
                  a(`href`:="#",
                    mkClass("selected") := Bind { filter() == x },
                    `click` listen { event =>
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
              `id` := "clear-completed",
              mkShow := Bind { numCompleted() > 0 },
              `click` listen { event =>
                todos.remove(_.status() != Completed)
              },
              Bind {
                val doneString = numCompleted()
                s"Clear completed ($doneString)"
              }
            )
          )
        )
      )
    }

    dom.document.body.appendChild(component.nativeElement)
    dom.document.body.appendChild(
      footer(`id` := "info",
        p("Double-click to edit a todo")
      ).nativeElement
    )
  }


}