package moorka.flow.ops

import moorka.death.Reaper
import moorka.flow._
import moorka.flow.immutable.{Killer, Val, Dummy, FlatMap}

import scala.collection.immutable.Queue
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Success, Failure, Try}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class FlowOps[A](val self: Flow[A]) extends AnyVal {

  import MergeHelper._

  /**
    * @see filter
    */
  def withFilter(f: A ⇒ Boolean): Flow[A] = {
    self flatMap { x ⇒
      if (f(x)) Val(x)
      else Dummy
    }
  }

  /**
    * Creates new binding applying `f` to values.
    * @return
    */
  def filter(f: A ⇒ Boolean): Flow[A] = {
    withFilter(f)
  }

  /**
    * Creates new binding applying `f` to values
    */
  def map[B](f: A ⇒ B): Flow[B] = {
    self flatMap { x ⇒
      Val(f(x))
    }
  }

  def onValue[U](f: Try[A] ⇒ U)(implicit ctx: Context, reaper: Reaper): Sink[A] = {
    reaper mark {
      val successHandler = (x: A) ⇒ f(Success(x))
      val failureHandler = (x: Throwable) ⇒ f(Failure(x))
      Sink(self, successHandler, failureHandler, once = false)
    }
  }

  def andThen[U](f: PartialFunction[A, U])(implicit ctx: Context, reaper: Reaper): Flow[A] = {
    reaper.mark(Sink(self, f, ctx.globalErrorHandler, once = false))
    self
  }

  def foreach[U](f: A ⇒ U)(implicit ctx: Context, reaper: Reaper): Sink[A] = {
    reaper.mark(Sink(self, f, ctx.globalErrorHandler, once = false))
  }

  def once[U](f: A ⇒ U)(implicit ctx: Context, reaper: Reaper): Sink[A] = {
    reaper.mark(Sink(self, f, ctx.globalErrorHandler, once = true))
  }

  def until(f: A ⇒ Boolean): Flow[Unit] = {
    self flatMap { x ⇒
      if (!f(x)) Killer
      else Dummy
    }
  }

  def filterNot(f: A ⇒ Boolean): Flow[A] = {
    self.filter(x ⇒ !f(x))
  }


  def zip[B](wth: Flow[B]): Flow[(A, B)] = {
    self flatMap { a ⇒
      wth flatMap { b ⇒
        Val((a, b))
      }
    }
  }

  def drop(num: Int): Flow[A] = {
    var drops = 0
    self flatMap { x ⇒
      if (drops < num) {
        drops += 1
        Dummy
      }
      else {
        Val(x)
      }
    }
  }

  def take(count: Int): Flow[Seq[A]] = {
    val seqs = self.fold(Queue.empty[A]) {
      case (acc, x) if acc.length == count ⇒ Queue(x)
      case (acc, x) ⇒ acc.enqueue(x)
    }
    seqs.filter(_.length == count)
  }

  def fold[B](z: B)(op: (B, A) => B): Flow[B] = {
    var state = z
    self map { x ⇒
      val newValue = op(state, x)
      state = newValue
      newValue
    }
  }

  def or[B](b: Flow[B]): Flow[Either[A, B]] = {
    self.map[Either[A, B]](Left(_)).
      merge(b.map(Right(_)))
  }

  def recover(pf: PartialFunction[Throwable, A]): Flow[A] = {
    new Flow[A] {
      def pull[U](f: FlowAtom[A] ⇒ U): Unit = {
        self pull {
          case FlowAtom.Fail(tr) if pf.isDefinedAt(tr) ⇒
            f(FlowAtom.Value(pf(tr)))
          case els ⇒ f(els)
        }
      }
      def flatMap[B](f: A ⇒ Flow[B]): Flow[B] = {
        new FlatMap(this, f)
      }
    }
  }

  def collect[B](pf: PartialFunction[A, B]): Flow[B] = {
    self flatMap { x ⇒
      if (pf.isDefinedAt(x)) Val(pf(x))
      else Dummy
    }
  }

  def switch[L, R](f: A ⇒ Either[L, R]): (Flow[L], Flow[R]) = {
    val either = self.map(f)
    val left = either collect { case Left(x) ⇒ x }
    val right = either collect { case Right(x) ⇒ x }
    (left, right)
  }

  def merge(b: Flow[A]): Flow[A] = new Flow[A] {
    def pull[U](f: FlowAtom[A] ⇒ U): Unit = {
      self.pull(f)
      b.pull(f)
    }
    def flatMap[B](f: A ⇒ Flow[B]): Flow[B] = {
      new FlatMap(this, f)
    }
  }

  def mergeUnimportant(b: Flow[_]): Flow[Unit] = new Flow[Unit] {
    def pull[U](f: FlowAtom[Unit] ⇒ U): Unit = {
      self.pull(_ ⇒ pullUnit(f))
      b.pull(_ ⇒ pullUnit(f))
    }
    def flatMap[B](f: Unit ⇒ Flow[B]): Flow[B] = {
      new FlatMap(this, f)
    }
  }

  def partition(f: A ⇒ Boolean): (Flow[A], Flow[A]) = {
    val left = self.filter(f)
    val right = self.filterNot(f)
    (left, right)
  }

  def toFuture(implicit ctx: Context, reaper: Reaper): Future[A] = {
    val promise = Promise[A]()
    val sink = Sink(self, promise.success, promise.failure, once = true)
    reaper.mark(sink)
    promise.future
  }
}

private object MergeHelper {
  val unitAtom = FlowAtom.Value(())
  def pullUnit[U](f: FlowAtom[Unit] ⇒ U): Unit = {
    f(unitAtom)
  }
}
