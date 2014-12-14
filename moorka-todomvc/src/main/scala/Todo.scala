/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
case class Todo(txt: String, status: Status)

sealed trait Status

sealed trait Filter

case object Active extends Status with Filter

case object Completed extends Status with Filter

case object Editing extends Status

case object All extends Filter