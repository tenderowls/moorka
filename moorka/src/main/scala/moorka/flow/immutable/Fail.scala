package moorka.flow.immutable

import moorka.flow.{Flow, FlowAtom}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
final case class Fail(cause: Throwable) extends Flow[Nothing] {
  lazy val atom = FlowAtom.Fail(cause)
  def pull[U](f: FlowAtom[Nothing] => U): Unit = f(atom)
  def flatMap[B](f: (Nothing) => Flow[B]): Flow[B] = this
}
