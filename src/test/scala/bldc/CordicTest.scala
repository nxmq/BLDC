//SPDX-License-Identifier: 0BSD
//Copyright (c) 2020 Nicolas Machado
//
//Permission to use, copy, modify, and/or distribute this software for any
//purpose with or without fee is hereby granted.
//
//THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
//REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
//AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
//INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
//LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
//OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
//PERFORMANCE OF THIS SOFTWARE.
package bldc

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest._
import bldc.util.RotationCordic

class CordicSpec extends FlatSpec with Matchers {
  behavior of "Cordic"

  it should "compute sin and cos properly" in {
    iotesters.Driver.execute(
      args = Array("--backend-name",
        "verilator",
        "--generate-vcd-output",
        "on",
        "--target-dir",
        "test_build",
        "-tn",
        "test_build",
        "--no-dce"),dut = () => new RotationCordic(13,18,15 ,20,16)) { c =>
      new CordicTester(c)
    } should be(true)
  }
}

class CordicTester(c: RotationCordic) extends PeekPokeTester(c) {
  System.err.print("cordic gain is:")
  System.err.println(c.getCordicGain.formatted("%f"))
  poke(c.io.ix, Math.round((1.0/(c.getCordicGain*(1<<(c.inputBits-1))-1))))
  poke(c.io.iy, 0)
  poke(c.io.ce, true.B)
  poke(c.io.iaux, true.B)
  poke(c.io.ph, 131072)
  for(i <- 1 to c.stageCount*2) {
    step(1)
  }
  expect(c.io.oaux,true.B)
}