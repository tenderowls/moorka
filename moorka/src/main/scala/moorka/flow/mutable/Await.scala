package moorka.flow.mutable

import moorka.death.Reaper
import moorka.flow._
import moorka.flow.immutable.Bindable

object Await {
  def apply[T](source: Flow[T])(implicit ctx: Context, reaper: Reaper = Reaper.nice): Flow[T] = {
    reaper.mark(new Await[T](ctx, source))
  }
}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
private class Await[T](val ctx: Context, val source: Flow[T]) extends Sink[T] with Flow[T] with Bindable[T] {

  type Puller[U] = FlowAtom[T] ⇒ U

  var pullers = List.empty[Puller[_]]

  val once = true

  val successHandler = { value: T ⇒
    for (puller ← pullers)
      puller(FlowAtom.Value(value))
  }

  val failHandler = { cause: Throwable ⇒
    for (puller ← pullers)
      puller(FlowAtom.Fail(cause))
  }

  def pull[U](f: Puller[U]): Unit = source pull {
    case FlowAtom.Empty ⇒
      pullers ::= f
    case atom ⇒
      f(atom)
      kill()
  }
}
