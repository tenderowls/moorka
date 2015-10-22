package moorka.flow.immutable

import moorka.flow.{Flow, FlowAtom}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
final case class Val[+A](data: A) extends Flow[A] {
  lazy val atom = FlowAtom.Value(data)
  def pull[U](f: FlowAtom[A] => U): Unit = f(atom)
  def flatMap[B](f: A ⇒ Flow[B]): Flow[B] = {
    try {
      f(data)
    } catch {
      case cause: Throwable ⇒
        Fail(cause)
    }
  }
}
