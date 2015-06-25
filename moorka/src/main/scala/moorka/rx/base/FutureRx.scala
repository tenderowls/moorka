package moorka.rx.base

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class FutureRx[A](rx: Rx[A]) extends Future[A] {

  private val reaper = moorka.rx.Reaper()
  private var savedValue: Option[A] = None

  reaper mark {
    rx once { x ⇒
      savedValue = Some(x)
    }
  }

  def onComplete[U](f: (Try[A]) => U)(implicit executor: ExecutionContext): Unit = {
    savedValue match {
      case Some(x) ⇒ f(Success(x))
      case None ⇒ reaper mark {
        rx once { x ⇒
          f(Success(x))
        }
      }
    }
  }

  override def foreach[U](f: (A) => U)(implicit executor: ExecutionContext): Unit = {
    savedValue match {
      case Some(x) ⇒ f(x)
      case None ⇒ reaper mark {
        rx once { x ⇒
          f(x)
        }
      }
    }
  }


  def isCompleted: Boolean = savedValue.isDefined

  def value: Option[Try[A]] = savedValue.map(Success(_))

  @throws[Exception](classOf[Exception])
  def result(atMost: Duration)(implicit permit: CanAwait): A = ???

  @throws[InterruptedException](classOf[InterruptedException])
  @throws[TimeoutException](classOf[TimeoutException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = ???
}
