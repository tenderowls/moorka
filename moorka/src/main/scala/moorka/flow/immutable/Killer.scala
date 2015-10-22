package moorka.flow.immutable

import moorka.flow.{Flow, FlowAtom}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
case object Killer extends Flow[Nothing] {
  def pull[U](f: FlowAtom[Nothing] => U): Unit = f(FlowAtom.End)
  def flatMap[B](f: Nothing ⇒ Flow[B]): Flow[B] = this
}
