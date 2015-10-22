package moorka.death

object Mortal {
  def immortal: Mortal = new Mortal {
    def kill(): Unit = ()
  }
}
/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Mortal {
  def kill(): Unit
}
