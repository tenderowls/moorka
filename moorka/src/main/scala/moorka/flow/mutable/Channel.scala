package moorka.flow.mutable

import moorka.flow._
import moorka.flow.immutable.{Bindable, FlatMap}

import scala.collection.immutable.Queue

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Channel {
  def apply[T](implicit ctx: Context): Channel[T] = new Channel[T](ctx)
}

class Channel[T](protected val context: Context) extends Flow[T] with Bindable[T] with Source {

  private val killer = context.addSource(this)
  private var data = Queue.empty[T]

  def validate(): Unit = {
    data = Queue.empty[T]
  }

  def push(value: T): Unit = {
    data = data.enqueue(value)
    context.invalidate()
  }

  override def pull[U](f: FlowAtom[T] ⇒ U): Unit = {
    if (data.nonEmpty) {
      for (value ← data)
        f(FlowAtom.Value(value))
    } else {
      f(FlowAtom.Empty)
    }
  }

  def kill(): Unit = killer()
}
