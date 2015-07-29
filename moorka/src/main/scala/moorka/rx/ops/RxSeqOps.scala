package moorka.rx.ops

import moorka._

import scala.collection.mutable

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class RxSeqOps[A](val self: Rx[Seq[A]]) extends AnyVal {

  implicit def toBuffer: BufferView[A] = {
    new Buffer[A](mutable.Buffer.empty[A]) {
      val subscription = self foreach { s ⇒
        s.length match {
          case l if l > this.length ⇒
            val startIndex = this.length
            for (i ← 0 until l - startIndex)
              this += s(startIndex + i)
          case l if l < this.length ⇒
            for (i ← 0 until this.length - l)
              this.remove(0)
          case _ ⇒
        }
        for (i ← s.indices) {
          this(i) = s(i)
        }
      }
    }
  }

}
