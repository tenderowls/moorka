package moorka.rx.death

object Reaper {

  /**
   * This nice version of death reaper doesn't
   * care about mortals. Only flowers.
   */
  val nice = new Reaper {
    def sweep(): Unit = {}

    def mark[T <: Mortal](mortal: T): T = mortal
  }

  def apply() = new Reaper() {

    @volatile
    private var mortals: List[Mortal] = Nil

    def mark[T <: Mortal](mortal: T): T = {
      mortals = mortal :: mortals collect {
        case value: Alive if value.alive ⇒ value
        case value ⇒ value
      }
      mortal
    }

    def sweep() = {
      mortals.foreach(_.kill())
    }
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Reaper {

  def mark(mortals: Mortal*): Unit = {
    for (x ← mortals) {
      mark(x)
    }
  }

  def mark[T <: Mortal](mortal: T): T

  def sweep(): Unit
}
