package moorka.rx.base

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Var {
  def apply[A](x: A) = new Var[A](x)
}

class Var[A](protected var value: A) extends Emitter[A] with RxState[A] {

  def apply(): A = {
    value
  }

  def update(x: A): Unit = {
    if (x != value) {
      value = x
      emit(x)
    }
  }
}
