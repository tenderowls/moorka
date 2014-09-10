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

    val todosView = Repeat[Task](dataProvider = Var(todos.view), itemRenderer = { todo: Task =>
      val inputRef = input(`class` := "edit", `value` := todo.txt())
      li(
        mkClass("completed") := Bind { todo.status() == Completed },
        mkClass("editing") := Bind { todo.status() == Editing },
        div(`class` := "view",
          `on-double-click` := { (event: dom.Event) =>
            if (todo.status() == Active && !nowEditing()) {
              todo.status() = Editing
              nowEditing() = true
            }
          },
          input(
            `class` := "toggle",
            `type` := "checkbox",
            `style` := "cursor: pointer",
            `on-click` := { (event: dom.Event) =>
              todo.status() = todo.status() match {
                case Active => Completed
                case Completed => Active
                case Editing => Active
              }
              refreshCounters()
            },
            `checked` := todo.status.map {
              x: Status => x == Completed
            }
          ),
          label(todo.txt),
          button(
            `class` := "destroy",
            `style` := "cursor: pointer",
            `on-click` := { (event: dom.Event) =>
              todos -= todo
            }
          )
        ),
        form(
          `on-submit` := { (event: dom.Event) =>
            event.preventDefault()
            val x = `value` extractFrom inputRef
            nowEditing() = false
            todo.status() = Active
            todo.txt() = x
            false
          },
          inputRef
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
            `on-submit` := { (event: dom.Event) =>
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
            `checked` := Bind { numCompleted() == todos.length },
            `on-click` := { (event:dom.Event) =>
              val newStatus = event.target.asInstanceOf[js.Dynamic].checked.asInstanceOf[Boolean] match {
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
              strong( Bind { numActive().toString }),
              span(" item left")
            ),
            ul(`id` := "filters",
              List(All, Active, Completed).map { x =>
                li(
                  a(`href`:="#",
                    mkClass("selected") := Bind { filter() == x },
                    `on-click` := { (event:dom.Event) =>
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
              mkShow := Bind {
                println(numCompleted() > 0)
                numCompleted() > 0
              },
              `on-click` := { (event: dom.Event) =>
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