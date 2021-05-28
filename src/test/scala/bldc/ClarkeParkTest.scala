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

class ClarkeParkSpec extends FlatSpec with Matchers {
  behavior of "ClarkePark"

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
        "--no-dce"),dut = () => new ClarkeParkTransform(12,11,14)) { c =>
      new ClarkeParkTester(c)
    } should be(true)
  }
}

class ClarkeParkTester(c: ClarkeParkTransform) extends PeekPokeTester(c) {
  poke(c.io.iu,531)
  poke(c.io.iv,126)
  poke(c.io.phase,1202)
  poke(c.io.ivalid,true)
  step(1)
  for(i <- 0 to c.outBits) {
    step(1)
  }
  expect(c.io.ovalid,true.B)
}