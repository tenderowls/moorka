package moorka.rx.bindings

import moorka.rx.{Dummy, Rx, Source}
import moorka.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[moorka] class StatelessBinding[From, To](val parent: Source[From],
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

  override private[moorka] def killUpstreams(): Unit = {
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
