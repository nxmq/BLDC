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
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

class SpaceVectorPWM extends Module {
  val counterSize: Int = 16
  val cntMid: UInt = ((1 << counterSize - 1) - 1).U((counterSize+1).W)
  val cntMax: UInt = ((1 << counterSize) - 1).U((counterSize+1).W)

  val io = IO(new Bundle {
    //DC-LINK NORMALIZED TARGET VOLTAGE (0-hFFFF mapped to 0-1)
    val voltage: UInt = Input(UInt(16.W))
    //TARGET PHASE (0-1536  mapped to 0-2PI)
    val phase: UInt = Input(UInt(11.W))

    //DIRECTION OF ROTATION (TRUE=CW, FALSE=CCW)
    val rotationDirection: Bool = Input(Bool())

    //OUTPUTS TO SWITCHES
    val uHighOn: Bool = Output(Bool())
    val uLowOn: Bool = Output(Bool())
    val vHighOn: Bool = Output(Bool())
    val vLowOn: Bool = Output(Bool())
    val wHighOn: Bool = Output(Bool())
    val wLowOn: Bool = Output(Bool())

    //DEBUG OUTPUTS
  //    val uDuty: UInt = Output(UInt(counterSize.W))
  //    val vDuty: UInt = Output(UInt(counterSize.W))
  //    val wDuty: UInt = Output(UInt(counterSize.W))
  //    val dx: UInt = Output(UInt(16.W))
  //    val dy: UInt = Output(UInt(16.W))
  //    val phaseSextant: UInt = Output(UInt(3.W))
  //    val phaseLocation: UInt = Output(UInt(8.W))
  //    val curMagX: UInt = Output(UInt(16.W))
  //    val curMagY: UInt = Output(UInt(16.W))
  })


  val phasorMagTableX: Mem[UInt] = Mem(256, UInt(16.W))
  loadMemoryFromFile(phasorMagTableX, "phasormagx.txt", MemoryLoadFileType.Hex)
  val phasorMagTableY: Mem[UInt] = Mem(256, UInt(16.W))
  loadMemoryFromFile(phasorMagTableY, "phasormagy.txt", MemoryLoadFileType.Hex)
  val counter: UpDownCounter = Module(new UpDownCounter(counterSize))
  val uDuty: UInt = RegInit(65535.U(16.W))
  val vDuty: UInt = RegInit(65535.U(16.W))
  val wDuty: UInt = RegInit(65535.U(16.W))
  val phaseSextant : UInt = RegInit(0.U(3.W))
  val phaseLocation: UInt = RegInit(0.U(8.W))
  val curMagX: UInt = RegInit(0.U(16.W))
  val curMagY: UInt = RegInit(0.U(16.W))
  val dx: UInt = RegInit(0.U(16.W))
  val dy: UInt = RegInit(0.U(16.W))

//  io.uDuty := uDuty
//  io.vDuty := vDuty
//  io.wDuty := wDuty
//  io.dx := dx
//  io.dy := dy
//  io.phaseSextant := phaseSextant
//  io.phaseLocation := phaseLocation
//  io.curMagX := curMagX
//  io.curMagY := curMagY

  when(counter.io.cnt === 0.U) {
    phaseLocation := io.phase(7,0)
    phaseSextant := io.phase(10,8)
  }
  when(counter.io.cnt === 1.U && counter.io.dir) {
    curMagX := phasorMagTableX.read(phaseLocation)
    curMagY := phasorMagTableY.read(phaseLocation)
  }
  when(counter.io.cnt === 2.U && counter.io.dir) {
    dx := (curMagX * io.voltage)(31,16)
    dy := (curMagY * io.voltage)(31,16)
  }
  when(counter.io.cnt === 3.U && counter.io.dir) {
    when(phaseSextant === 0.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax - dx - dy) >> 1
        vDuty := (cntMax + dx - dy) >> 1
        wDuty := (cntMax + dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMax + dx + dy) >> 1
        vDuty := (cntMax - dx - dy) >> 1
        wDuty := (cntMax - dx + dy) >> 1
      }
    }.elsewhen(phaseSextant === 1.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax - dx + dy) >> 1
        vDuty := (cntMax - dx - dy) >> 1
        wDuty := (cntMax + dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMax + dx + dy) >> 1
        vDuty := (cntMax + dx - dy) >> 1
        wDuty := (cntMax - dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 2.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax + dx + dy) >> 1
        vDuty := (cntMax - dx - dy) >> 1
        wDuty := (cntMax + dx - dy) >> 1
      }.otherwise {
        uDuty := (cntMax - dx + dy) >> 1
        vDuty := (cntMax + dx + dy) >> 1
        wDuty := (cntMax + dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 3.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax + dx + dy) >> 1
        vDuty := (cntMax - dx + dy) >> 1
        wDuty := (cntMax - dx - dy) >> 1
      }.otherwise {
        uDuty := (cntMax - dx - dy) >> 1
        vDuty := (cntMax + dx + dy) >> 1
        wDuty := (cntMax + dx - dy) >> 1
      }
    }.elsewhen(phaseSextant === 4.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax + dx - dy) >> 1
        vDuty := (cntMax + dx + dy) >> 1
        wDuty := (cntMax - dx - dy) >> 1
      }.otherwise {
        uDuty := (cntMax - dx - dy) >> 1
        vDuty := (cntMax - dx + dy) >> 1
        wDuty := (cntMax + dx + dy) >> 1
      }
    }.elsewhen(phaseSextant === 5.U) {
      when(io.rotationDirection) {
        uDuty := (cntMax - dx - dy) >> 1
        vDuty := (cntMax + dx + dy) >> 1
        wDuty := (cntMax - dx + dy) >> 1
      }.otherwise {
        uDuty := (cntMax + dx - dy) >> 1
        vDuty := (cntMax - dx - dy) >> 1
        wDuty := (cntMax + dx + dy) >> 1
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
