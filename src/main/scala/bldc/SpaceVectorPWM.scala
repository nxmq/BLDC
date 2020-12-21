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
import chisel3.experimental.FixedPoint

class SpaceVectorPWM extends Module {
  val counterSize: Int = 16
  val cntMid: UInt = ((1 << counterSize-1) -1).U(counterSize.W)
  val io = IO(new Bundle {
    //MODULATION INDEX
    val modidx: FixedPoint = Input(FixedPoint(12.W,10.BP))

    //DC-LINK NORMALIZED TARGET VOLTAGE (0-hFFFF mapped to 0-1)
    val voltage: UInt = Input(UInt(16.W))
    //TARGET PHASE (0-1536  mapped to 0-2PI)
    val phase: UInt = Input(UInt(16.W))

    //DIRECTION OF ROTATION (TRUE=CW, FALSE=CCW)
    val rotationDirection: Bool = Input(Bool())

    //OUTPUTS TO SWITCHES
    val uHighOn: Bool = Output(Bool())
    val uLowOn: Bool = Output(Bool())
    val vHighOn: Bool = Output(Bool())
    val vLowOn: Bool = Output(Bool())
    val wHighOn: Bool = Output(Bool())
    val wLowOn: Bool = Output(Bool())
  })

  def genPhasorTable(): Vec[UInt] = {
    VecInit((0 to 256).map {
      v => {
        val t1 = v * (math.Pi / (3.0 * 256.0))
        val t2 = (math.Pi / (3.0)) - t1
        val res1 = math.round(65535*((3 * math.cos(t1) - math.sqrt(3)*math.sin(t1))/3.0)).U(16.W)
        val res2 = math.round(65535*((3 * math.cos(t2) - math.sqrt(3)*math.sin(t2))/3.0)).U(16.W)
        res1 ## res2
      }
    })
  }

  val basis1: UInt = Reg(UInt(3.W))
  val basis2: UInt = Reg(UInt(3.W))
  val phasorMagTable: Vec[UInt] = genPhasorTable()
  val counter: UpDownCounter = Module(new UpDownCounter(counterSize))
  val reloadSig: Bool = counter.io.dir && !RegNext(counter.io.dir)
  val uDuty: UInt = Reg(UInt(counterSize.W))
  val vDuty: UInt = Reg(UInt(counterSize.W))
  val wDuty: UInt = Reg(UInt(counterSize.W))
  when(reloadSig) {
    val phaseSextant : UInt = ((io.phase >> 1) * 12289.U) >> 26
    val phaseLocation : UInt = io.phase - (phaseSextant * 10922.U)
    val dx = (phasorMagTable(phaseLocation).head(16).asUInt() * io.voltage).head(16)
    val dy = (phasorMagTable(phaseLocation).tail(16).asUInt() * io.voltage).head(16)

    when(phaseSextant === 0.U) {
      basis1 := 1.U
      basis2 := 3.U
      when(io.rotationDirection) {
        uDuty := (cntMid - dx - dy) >> 1
        vDuty := (cntMid + dx - dy) >> 1
        wDuty := (cntMid + dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMid + dx + dy) >> 1
        vDuty := (cntMid - dx - dy) >> 1
        wDuty := (cntMid - dx + dy) >> 1
      }
    }.elsewhen(phaseSextant === 1.U) {
      basis1 := 3.U
      basis2 := 2.U
      when(io.rotationDirection) {
        uDuty := (cntMid - dx + dy) >> 1
        vDuty := (cntMid - dx - dy) >> 1
        wDuty := (cntMid + dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMid + dx + dy) >> 1
        vDuty := (cntMid + dx - dy) >> 1
        wDuty := (cntMid - dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 2.U) {
      basis1 := 2.U
      basis2 := 6.U
      when(io.rotationDirection) {
        uDuty := (cntMid + dx + dy) >> 1
        vDuty := (cntMid - dx - dy) >> 1
        wDuty := (cntMid + dx - dy) >> 1
      }.otherwise {
        uDuty := (cntMid - dx + dy) >> 1
        vDuty := (cntMid + dx + dy) >> 1
        wDuty := (cntMid + dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 3.U) {
      basis1 := 6.U
      basis2 := 4.U
      when(io.rotationDirection) {
        uDuty := (cntMid + dx + dy) >> 1
        vDuty := (cntMid - dx + dy) >> 1
        wDuty := (cntMid - dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMid - dx - dy) >> 1
        vDuty := (cntMid + dx + dy) >> 1
        wDuty := (cntMid + dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 4.U) {
      basis1 := 4.U
      basis2 := 5.U
      when(io.rotationDirection) {
        uDuty := (cntMid + dx - dy) >> 1
        vDuty := (cntMid + dx + dy) >> 1
        wDuty := (cntMid - dx - dy) >> 1
      }.otherwise {
        uDuty := (cntMid - dx - dy) >> 1
        vDuty := (cntMid - dx + dy) >> 1
        wDuty := (cntMid + dx + dy) >> 1
      }
    }.elsewhen(phaseSextant === 5.U) {
      basis1 := 5.U
      basis2 := 1.U
      when(io.rotationDirection) {
        uDuty := (cntMid - dx - dy) >> 1
        vDuty := (cntMid + dx + dy) >> 1
        wDuty := (cntMid - dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMid + dx - dy) >> 1
        vDuty := (cntMid - dx - dy) >> 1
        wDuty := (cntMid + dx + dy) >> 1
      }    }

  }

  counter.io.en := true.B
  io.uHighOn :=  counter.io.cnt > uDuty
  io.uLowOn := counter.io.cnt <= uDuty
  io.vHighOn :=  counter.io.cnt > vDuty
  io.vLowOn := counter.io.cnt <= vDuty
  io.wHighOn :=  counter.io.cnt > wDuty
  io.wLowOn := counter.io.cnt <= wDuty

}
