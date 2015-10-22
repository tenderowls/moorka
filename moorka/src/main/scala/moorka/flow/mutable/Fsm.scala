package moorka.flow.mutable

import moorka.flow.immutable.Bindable
import moorka.flow.{Flow, FlowAtom}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object Fsm {

  def apply[T](initialValue: T, ignoreStateEquality: Boolean)(f: T ⇒ Flow[T]): Flow[T] = {
    new Fsm(initialValue, ignoreStateEquality, f)
  }

  def apply[T](initialValue: T)(f: T ⇒ Flow[T]): Flow[T] = {
    new Fsm(initialValue, false, f)
  }
}

private final class Fsm[T](var data: T, ignoreStateEquality: Boolean, lambda: T ⇒ Flow[T])
  extends Flow[T] with Bindable[T] {

  def updateAndContinue(newData: T, f: FlowAtom[T] ⇒ _): Unit = {
    data = newData
    f(FlowAtom.Value(newData))
  }

  def pull[U](f: FlowAtom[T] ⇒ U): Unit = {
    lambda(data).pull {
      case x: FlowAtom.Fail ⇒ f(x)
      case FlowAtom.Value(x) if ignoreStateEquality || x != data ⇒
        updateAndContinue(x, f)
      case FlowAtom.End ⇒ f(FlowAtom.End)
      case _ ⇒ f(FlowAtom.Empty)
    }
  }
}
