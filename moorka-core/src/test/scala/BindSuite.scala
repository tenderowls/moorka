import moorka.rx._
import utest._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object BindSuite extends TestSuite {
  val tests = TestSuite {
    "Binding expression" - {
      "must be updated when any disclosed State in the block changes" - {
        val a = Var(2)
        val b = Var(2)
        val binding = Bind { a() + b() }
        assert(binding() == 4)
        a() = 4; assert(binding() == 6)
        b() = 4; assert(binding() == 8)
      }
      "must not be updated if one of disclosed State wrapped in function" - {
        val a = Var(2)
        val b = Var(2)
        val binding = Bind {
          a() + (() => b())()
        }
        assert(binding() == 4)
        a() = 4; assert(binding() == 6)
        b() = 4; assert(binding() == 6)
      }
      "must not be updated if one of disclosed State wrapped in method" - {
        val a = Var(2)
        val b = Var(2)
        val binding = Bind {
          def f() = b()
          a() + f()
        }
        assert(binding() == 4)
        a() = 4; assert(binding() == 6)
        b() = 4; assert(binding() == 6)
      }
    }
  }
}
