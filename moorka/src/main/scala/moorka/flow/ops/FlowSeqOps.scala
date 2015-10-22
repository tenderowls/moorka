package moorka.flow.ops

import moorka.flow.Flow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class FlowSeqOps[A](val self: Flow[Seq[A]]) extends AnyVal {

//  def adjusted(implicit reaper: Reaper): Flow[A] = {
//    val channel = Channel[A]()
//    self foreach { xs ⇒
//      xs.foreach(x ⇒ channel.pull(Val(x)))
//    }
//    channel
//  }
//
//  def toBuffer: BufferView[A] = {
//    new Buffer[A](mutable.Buffer.empty[A]) {
//      val subscription = self foreach { s ⇒
//        s.length match {
//          case l if l > this.length ⇒
//            val startIndex = this.length
//            for (i ← 0 until l - startIndex)
//              this += s(startIndex + i)
//          case l if l < this.length ⇒
//            for (i ← 0 until this.length - l)
//              this.remove(0)
//          case _ ⇒
//        }
//        for (i ← s.indices) {
//          this(i) = s(i)
//        }
//      }
//    }
//  }

}
