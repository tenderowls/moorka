package moorka.rx.base.ops

import moorka.rx._
import moorka.rx.base.Var

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class StateOptionOps[T](val self: State[Option[T]]) extends AnyVal {

  def withDefault(default: T)(implicit reaper: Reaper = Reaper.nice): State[T] = {
    val x = new Var(default) {
      self subscribe {
        case Some(v) => update(v)
        case None =>
      }
    }
    reaper.mark(x)
    x
  }
}
