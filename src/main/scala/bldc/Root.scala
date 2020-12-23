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

class Root extends Module {
  var io = IO(new Bundle{
    //ENCODER INPUTS
    val quadEncA: Bool = Input(Bool())
    val quadEncB: Bool = Input(Bool())
    val quadEncI: Bool = Input(Bool())

    //HALL SENSOR INPUTS
    val hallSwitchU: Bool = Input(Bool())
    val hallSwitchV: Bool = Input(Bool())
    val hallSwitchW: Bool = Input(Bool())

    //SPI CONFIG IF INPUTS
    val spiCfgSCK: Clock = Input(Clock())
    val spiCfgCIPO: Bool = Input(Bool())
    val spiCfgCOPI: Bool = Input(Bool())
    val spiCfgCS: Bool = Input(Bool())

    // SWITCH OUTPUTS
    val uHighOn: Bool = Output(Bool())
    val uLowOn: Bool = Output(Bool())
    val vHighOn: Bool = Output(Bool())
    val vLowOn: Bool = Output(Bool())
    val wHighOn: Bool = Output(Bool())
    val wLowOn: Bool = Output(Bool())
  })

  val quadEnc: QuadratureEncoder = Module(new QuadratureEncoder(16,20,20))
  quadEnc.io.a := io.quadEncA
  quadEnc.io.b := io.quadEncB
  quadEnc.io.i := io.quadEncI

  val spaceVectorPWM: SpaceVectorPWM = Module(new SpaceVectorPWM())
  io.uHighOn := spaceVectorPWM.io.uHighOn
  io.uLowOn := spaceVectorPWM.io.uLowOn
  io.vHighOn := spaceVectorPWM.io.vHighOn
  io.vLowOn := spaceVectorPWM.io.vLowOn
  io.wHighOn := spaceVectorPWM.io.wHighOn
  io.wLowOn := spaceVectorPWM.io.wLowOn
}
