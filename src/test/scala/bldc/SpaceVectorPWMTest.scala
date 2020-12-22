package bldc


import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._

class LoadMemoryFromFileSpec extends FreeSpec with Matchers {
  "Initialization works at all" in {
    iotesters.Driver.execute(
      args = Array("--backend-name", "verilator", "--top-name", "spacevec_init_test"),
      dut = () => new SpaceVectorPWM()
    ) { c =>
      new SpaceVectorPWMTest(c)
    } should be (true)
  }
}

class SpaceVectorPWMTest(c: SpaceVectorPWM) extends PeekPokeTester(c) {
  poke(c.io.voltage,0xFFFF)
  poke(c.io.phase,17294)
  poke(c.io.rotationDirection,true)
  println(f"phaseSextant is  ${peek(c.io.phaseSextant)}%x ")
  println(f"phaseLocation is  ${peek(c.io.phaseLocation)}%x ")
  println(f"curMagX is  ${peek(c.io.curMagX)}%x ")
  println(f"curMagY is  ${peek(c.io.curMagY)}%x ")
  println(f"dx is  ${peek(c.io.dx)}%x ")
  println(f"dy is  ${peek(c.io.dy)}%x ")
  println(f"uDuty is  ${peek(c.io.uDuty)}%x ")
  println(f"vDuty is  ${peek(c.io.vDuty)}%x ")
  println(f"wDuty is  ${peek(c.io.wDuty)}%x ")
  step(1)
  println("step!")
  println(f"phaseSextant is  ${peek(c.io.phaseSextant)}%x ")
  println(f"phaseLocation is  ${peek(c.io.phaseLocation)}%x ")
  println(f"curMagX is  ${peek(c.io.curMagX)}%x ")
  println(f"curMagY is  ${peek(c.io.curMagY)}%x ")
  println(f"dx is  ${peek(c.io.dx)}%x ")
  println(f"dy is  ${peek(c.io.dy)}%x ")
  println(f"uDuty is  ${peek(c.io.uDuty)}%x ")
  println(f"vDuty is  ${peek(c.io.vDuty)}%x ")
  println(f"wDuty is  ${peek(c.io.wDuty)}%x ")
  step(1)
  println("step!")
  println(f"phaseSextant is  ${peek(c.io.phaseSextant)}%x ")
  println(f"phaseLocation is  ${peek(c.io.phaseLocation)}%x ")
  println(f"curMagX is  ${peek(c.io.curMagX)}%x ")
  println(f"curMagY is  ${peek(c.io.curMagY)}%x ")
  println(f"dx is  ${peek(c.io.dx)}%x ")
  println(f"dy is  ${peek(c.io.dy)}%x ")
  println(f"uDuty is  ${peek(c.io.uDuty)}%x ")
  println(f"vDuty is  ${peek(c.io.vDuty)}%x ")
  println(f"wDuty is  ${peek(c.io.wDuty)}%x ")
  step(1)
  println("step!")
  println(f"phaseSextant is  ${peek(c.io.phaseSextant)}%x ")
  println(f"phaseLocation is  ${peek(c.io.phaseLocation)}%x ")
  println(f"curMagX is  ${peek(c.io.curMagX)}%x ")
  println(f"curMagY is  ${peek(c.io.curMagY)}%x ")
  println(f"dx is  ${peek(c.io.dx)}%x ")
  println(f"dy is  ${peek(c.io.dy)}%x ")
  println(f"uDuty is  ${peek(c.io.uDuty)}%x ")
  println(f"vDuty is  ${peek(c.io.vDuty)}%x ")
  println(f"wDuty is  ${peek(c.io.wDuty)}%x ")
  step(1)
  println("step!")
  println(f"phaseSextant is  ${peek(c.io.phaseSextant)}%x ")
  println(f"phaseLocation is  ${peek(c.io.phaseLocation)}%x ")
  println(f"curMagX is  ${peek(c.io.curMagX)}%x ")
  println(f"curMagY is  ${peek(c.io.curMagY)}%x ")
  println(f"dx is  ${peek(c.io.dx)}%x ")
  println(f"dy is  ${peek(c.io.dy)}%x ")
  println(f"uDuty is  ${peek(c.io.uDuty)}%x ")
  println(f"vDuty is  ${peek(c.io.vDuty)}%x ")
  println(f"wDuty is  ${peek(c.io.wDuty)}%x ")
}