package moorka.flow.mutable

import moorka.flow._
import moorka.flow.immutable.FlatMap

import scala.collection.immutable.Queue

object Var {
  def apply[T](initialValue: T)(implicit context: Context): Var[T] = {
    new Var(context, initialValue)
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class Var[T](protected val context: Context, initialValue: T)
  extends Flow[T] with Source {

  private val killer = context.addSource(this)

  var changed = true
  var data = Queue(initialValue)

  def update(value: T): Unit = {
    if (value != data.last) {
      // Replace current data
      data = {
        if (changed) data.enqueue(value)
        else Queue(value)
      }
      context.invalidate()
      changed = true
    }
  }

  def validate(): Unit = {
    changed = false
    data = Queue(data.last)
  }

  def pull[U](f: FlowAtom[T] => U): Unit = {
    for (value ← data)
      f(FlowAtom.Value(value))
  }

  def flatMap[B](f: (T) => Flow[B]): Flow[B] = {
    new FlatMap(this, f)
  }

  def kill() = killer()
}
