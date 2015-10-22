package moorka.flow.immutable

import moorka.flow.{Flow, FlowAtom}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
final class FlatMap[From, To](from: Flow[From], lambda: From ⇒ Flow[To]) extends Flow[To] {

  def pull[U](f: FlowAtom[To] ⇒ U): Unit = from pull { value ⇒
    try {
      value match {
        case x: FlowAtom.Fail ⇒ f(x)
        case FlowAtom.Value(x) ⇒ lambda(x).pull(f)
        case FlowAtom.Empty ⇒ f(FlowAtom.Empty)
        case FlowAtom.End ⇒ f(FlowAtom.End)
      }
    } catch {
      case cause: Throwable ⇒
        f(FlowAtom.Fail(cause))
    }
  }

  def flatMap[B](f: To ⇒ Flow[B]): Flow[B] = {
    new FlatMap(this, f)
  }
}
