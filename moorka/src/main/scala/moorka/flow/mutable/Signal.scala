package moorka.flow.mutable

import moorka.flow.Context

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Signal[T](context: Context, value: T) extends Channel[T](context) {
  def fire(): Unit = push(value)
}

object Signal {

  def apply()(implicit context: Context): Signal[Unit] = {
    new Signal(context, ())
  }

  def apply[T](value: T)(implicit context: Context): Signal[T] = {
    new Signal(context, value)
  }
}
