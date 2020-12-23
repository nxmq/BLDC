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

class LoadMemoryFromFileSpec extends FreeSpec with Matchers {
  "Initialization works at all" in {
    iotesters.Driver.execute(
      args = Array("--backend-name",
    "verilator",
    "--generate-vcd-output",
    "on",
    "--target-dir",
    "test_build",
    "-tn",
    "test_build",
    "--no-dce"),
      dut = () => new SpaceVectorPWM()
    ) { c =>
      new SpaceVectorPWMTest(c)
    } should be (true)
  }
}

class SpaceVectorPWMTest(c: SpaceVectorPWM) extends PeekPokeTester(c) {
  poke(c.io.voltage,56753)
  poke(c.io.rotationDirection,false)
  poke(c.io.phase,196)
  for(i <- 1 to 5) {
    step(131060)
    poke(c.io.phase,196+256*i)
    step(12)
  }
  step(131060)

}