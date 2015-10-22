package moorka.flow.immutable

import moorka.flow.Flow

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
trait Bindable[+T] extends Flow[T] {
  def flatMap[B](f: T ⇒ Flow[B]): Flow[B] = {
    new FlatMap(this, f)
  }
}
