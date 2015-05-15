package moorka.rx.base

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxFuture[A](future: Future[A])
                       (implicit executionContext: ExecutionContext)
  extends Rx[Try[A]] {

  val st = Var[Option[Try[A]]](None)

  future onComplete { x ⇒
    st.update(Some(x))
    st.kill()
  }

  def flatMap[B](f: (Try[A]) => Rx[B]): Rx[B] = {
    st flatMap {
      case Some(x) ⇒ f(x)
      case None ⇒ Dummy
    }
  }

  def alive = st.alive 
  
  def kill(): Unit = {
    st.kill()
  }
}
