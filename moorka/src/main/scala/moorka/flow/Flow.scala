package moorka.flow

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
trait Flow[+A] {

  def pull[U](f: FlowAtom[A] ⇒ U): Unit

  /**
    * Creates new binding which will pull value
    * from Flow returned by `f`.
    */
  def flatMap[B](f: A ⇒ Flow[B]): Flow[B]
}
