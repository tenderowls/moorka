package moorka.rx.base.bindings

import moorka.rx.base.{Dummy, Rx, Source}
import moorka.rx.death.Reaper

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[rx] class Binding[From, To](val parent: Source[From],
                                    lambda: From ⇒ Rx[To]) extends Source[To] {

  val bindingContext: Option[Rx[_]] = bindingStack.headOption
  
  def run(x: From) = {
    // Cleanup upstreams
    val currentBindingStack = bindingStack
    currentBindingStack.push(this)
    killUpstreams()
    try {
      // Pull value from upstream
      pull(lambda(x))
    }
    finally {
      currentBindingStack.pop()
    }
  }

  parent.attachBinding(this)

  override private[rx] def killUpstreams(): Unit = {
    val optionThis = Some(this)
    upstreams foreach {
      case upstream: Binding[_, _] ⇒
        upstream.parent match {
          case binding: Binding[_, _] if binding.bindingContext == optionThis ⇒
            binding.kill()
          case _ ⇒ ()
        }
        upstream.kill()
      case x ⇒
        x.kill()
    }
  }

  override def kill(): Unit = {
    parent.detachBinding(this)
    super.kill()
  }

  def flatMap[B](f: (To) => Rx[B])(implicit reaper: Reaper = Reaper.nice): Rx[B] = {
    if (_alive) {
      reaper.mark(new Binding(this, f))
    }
    else {
      Dummy
    }
  }
}
