package moorka.flow

sealed trait FlowAtom[+T]

object FlowAtom {
  case class Value[+T](data: T) extends FlowAtom[T]
  case class Fail(cause: Throwable) extends FlowAtom[Nothing]
  case object Empty extends FlowAtom[Nothing]
  case object End extends FlowAtom[Nothing]
}
