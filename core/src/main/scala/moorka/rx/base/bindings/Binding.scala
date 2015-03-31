package moorka.rx.base.bindings

import moorka.rx.base.{Rx, Source}

import scala.ref.WeakReference

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
private[rx] class Binding[From, To](parent: Source[From],
                                    lambda: From ⇒ Rx[To]) extends Source[To] {

  def run(x: From) = {
    // Cleanup upstreams
    upstreams.foreach(_.kill())
    upstreams = Nil
    // Pull value from upstream
    pull(lambda(x))
  }

  val ref = WeakReference(this)
  parent.attachBinding(ref)

  override def kill(): Unit = {
    parent.detachBinding(ref)
    super.kill()
  }

  def flatMap[B](f: (To) => Rx[B]): Rx[B] = new Binding(this, f)
}