package moorka.rx.base.ops

import moorka.rx.Var
import moorka.rx.base.bindings.StatelessBinding
import moorka.rx.base.{Rx, Val}
import moorka.rx.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class VarOps[A](val self: Var[A]) extends AnyVal {

  def stateless(implicit reaper: Reaper = Reaper.nice): Rx[A] = {
    val binding = new StatelessBinding[A, A](self, x ⇒ Val(x))
    reaper.mark(binding)
    binding
  }
  
}
