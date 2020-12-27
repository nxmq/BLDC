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
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

class SpaceVectorPWM(val counterSize: Int = 14, val phaseResolution: Int = 8) extends Module {
  val cntMid: Int = ((1 << counterSize - 1) - 1)
  val cntMax: Int = ((1 << counterSize) - 1)

  val io = IO(new Bundle {
    //DC-LINK NORMALIZED TARGET VOLTAGE (0-cntMax mapped to 0-1)
    val voltage: UInt = Input(UInt(counterSize.W))
    //TARGET PHASE (0-6*phaseResolution  mapped to 0-2PI)
    val phase: UInt = Input(UInt((phaseResolution+3).W))

    //VALID SIGNALS
    val ivalid: Bool = Input(Bool())
    val ovalid: Bool = Output(Bool())

    //OUTPUTS TO SWITCHES
    val uHighOn: Bool = Output(Bool())
    val uLowOn: Bool = Output(Bool())
    val vHighOn: Bool = Output(Bool())
    val vLowOn: Bool = Output(Bool())
    val wHighOn: Bool = Output(Bool())
    val wLowOn: Bool = Output(Bool())
  })

  val phasorMagTableX: Mem[UInt] = Mem(256, UInt(16.W))
  loadMemoryFromFile(phasorMagTableX, "phasormagx.txt", MemoryLoadFileType.Hex)
  val phasorMagTableY: Mem[UInt] = Mem(256, UInt(16.W))
  loadMemoryFromFile(phasorMagTableY, "phasormagy.txt", MemoryLoadFileType.Hex)
  val counter: UpDownCounter = Module(new UpDownCounter(counterSize))
  val uDuty: UInt = RegInit(cntMax.U(counterSize.W))
  val vDuty: UInt = RegInit(cntMax.U(counterSize.W))
  val wDuty: UInt = RegInit(cntMax.U(counterSize.W))
  val phaseSextant : UInt = Reg(UInt(3.W))
  val phaseLocation: UInt = Reg(UInt(phaseResolution.W))
  val curMagX: UInt = Reg(UInt(counterSize.W))
  val curMagY: UInt = Reg(UInt(counterSize.W))
  val outputPulses: Vec[UInt] = Reg(Vec(4,UInt(counterSize.W)))
  val valid: Bool = RegInit(false.B)
  io.ovalid := valid
  when(io.ivalid) {
    when(counter.io.cnt === 0.U) {
      valid := false.B
      phaseLocation := io.phase(phaseResolution - 1, 0)
      phaseSextant := io.phase(phaseResolution + 2, phaseResolution)
    }
    when(counter.io.cnt === 1.U && counter.io.dir) {
      curMagX := phasorMagTableX(phaseLocation)
      curMagY := phasorMagTableY(phaseLocation)
    }
    when(counter.io.cnt === 2.U && counter.io.dir) {
      val dx = (curMagX * io.voltage) (counterSize * 2 - 1, counterSize)
      val dy = (curMagY * io.voltage) (counterSize * 2 - 1, counterSize)
      outputPulses(0) := (cntMax.U((counterSize + 1).W) - dx - dy) >> 1
      outputPulses(1) := (cntMax.U((counterSize + 1).W) + dx + dy) >> 1
      outputPulses(2) := (cntMax.U((counterSize + 1).W) - dx + dy) >> 1
      outputPulses(3) := (cntMax.U((counterSize + 1).W) + dx - dy) >> 1
    }
    when(counter.io.cnt === 3.U && counter.io.dir) {
      valid := true.B
      switch(phaseSextant) {
        is(0.U) {
          uDuty := outputPulses(1)
          vDuty := outputPulses(2)
          wDuty := outputPulses(0)
        }
        is(1.U) {
          uDuty := outputPulses(3)
          vDuty := outputPulses(1)
          wDuty := outputPulses(0)
        }
        is(2.U) {
          uDuty := outputPulses(0)
          vDuty := outputPulses(1)
          wDuty := outputPulses(2)
        }
        is(3.U) {
          uDuty := outputPulses(0)
          vDuty := outputPulses(3)
          wDuty := outputPulses(1)
        }
        is(4.U) {
          uDuty := outputPulses(2)
          vDuty := outputPulses(0)
          wDuty := outputPulses(1)
        }
        is(5.U) {
          uDuty := outputPulses(1)
          vDuty := outputPulses(0)
          wDuty := outputPulses(3)
        }
      }
    }
  }
  counter.io.en := true.B
  io.uHighOn := counter.io.cnt > uDuty
  io.uLowOn := counter.io.cnt <= uDuty
  io.vHighOn :=  counter.io.cnt > vDuty
  io.vLowOn := counter.io.cnt <= vDuty
  io.wHighOn := counter.io.cnt > wDuty
  io.wLowOn := counter.io.cnt <= wDuty
}
