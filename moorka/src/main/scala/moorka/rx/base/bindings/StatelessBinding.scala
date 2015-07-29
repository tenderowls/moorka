package moorka.rx.base.bindings

import moorka.rx.base.{Dummy, Rx, Source}
import moorka.rx.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[rx] class StatelessBinding[From, To](val parent: Source[From],
                                    lambda: From ⇒ Rx[To]) 
  extends Source[To]
  with Binding[From]
  with HasBindingContext[From] {

  def run(x: From): Unit = {
    withContext(upstreams) {
      pull(lambda(x))
    }
  }

  parent.attachBinding(this)

  override private[rx] def killUpstreams(): Unit = {
    killDependencies(upstreams)
  }

  override def kill(): Unit = {
    parent.detachBinding(this)
    super.kill()
  }

  def flatMap[B](f: (To) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (isAlive) {
      reaper.mark(new StatelessBinding(this, f))
    }
    else {
      Dummy
    }
  }
}
