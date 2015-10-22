package moorka.flow

import moorka.death.{Alive, Mortal}

object Sink {

  private class SinkImpl[T](val ctx: Context,
                            val source: Flow[T],
                            val successHandler: T ⇒ _,
                            val failHandler: Throwable ⇒ _,
                            val once: Boolean)
    extends Sink[T]

  def apply[T](source: Flow[T], successful: T ⇒ _, failure: Throwable ⇒ _, once: Boolean)
              (implicit ctx: Context): Sink[T] = {
    new SinkImpl[T](ctx, source, successful, failure, once)
  }
}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
trait Sink[T] extends Mortal with Alive {

  def ctx: Context

  def source: Flow[T]

  def successHandler: T ⇒ _

  def failHandler: Throwable ⇒ _

  def once: Boolean

  var alive = true

  protected var prev: FlowAtom[T] = FlowAtom.Empty

  def run(): Unit = {
    if (alive) {
      if (once) kill()
      source pull {
        case current: FlowAtom.Fail ⇒
          prev = current
          failHandler(current.cause)
        case current: FlowAtom.Value[T] if current != prev ⇒
          prev = current
          successHandler(current.data)
        case current ⇒
          prev = current
      }
    }
  }

  private val killer = ctx.addSink(this)

  def kill(): Unit = if (alive) {
    alive = false
    killer()
  }
}
