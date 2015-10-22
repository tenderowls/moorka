package moorka.flow

import moorka.flow.immutable.{Bindable, Dummy, Val, Fail}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Try, Success, Failure}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
package object converters {

  final class FutureFlow[A](future: Future[A])
                           (implicit executionContext: ExecutionContext) extends Flow[A] with Bindable[A] {

    type Puller[U] = FlowAtom[A] ⇒ U

    var pullers = List.empty[Puller[_]]

    private def fromTry(x: Try[A]): FlowAtom[A] = x match {
      case Success(v) ⇒ FlowAtom.Value(v)
      case Failure(t) ⇒ FlowAtom.Fail(t)
    }

    future onComplete { tr ⇒
      val atom = fromTry(tr)
      pullers.foreach(_(atom))
    }

    def pull[U](f: Puller[U]): Unit = {
      if (future.isCompleted) {
        future.value match {
          case Some(tr) ⇒ f(fromTry(tr))
          case _ ⇒ f(FlowAtom.Empty)
        }
      } else {
        pullers ::= f
      }
    }
  }

  final class FutureOps[+A](val future: Future[A]) extends AnyVal {
    def toFlow(implicit executionContext: ExecutionContext): Flow[A] = {
      if (future.isCompleted) {
        future.value match {
          case Some(Failure(throwable)) ⇒ Fail(throwable)
          case Some(Success(x)) ⇒ Val(x)
          case _ ⇒
            // TODO should be fail
            Dummy
        }
      }
      else {
        new FutureFlow[A](future)
      }
    }
  }

  implicit def future[T](x: Future[T]): FutureOps[T] = new FutureOps[T](x)
}
