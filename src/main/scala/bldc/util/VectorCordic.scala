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
package bldc.util

import chisel3._
import chisel3.util._

class VectorCordic(val inputBits: Int, val workingBits: Int, val magBits: Int, val phaseBits: Int, val stageCount: Int) extends Module {
  var io = IO(new Bundle{
    val ce: Bool = Input(Bool())
    val ix: SInt = Input(SInt(inputBits.W))
    val iy: SInt = Input(SInt(inputBits.W))
    val omg: SInt = Output(SInt(magBits.W))
    val oph: UInt = Output(UInt(phaseBits.W))
    val iaux: Bool = Input(Bool())
    val oaux: Bool = Output(Bool())
  })

  def getCordicGain: Double = {
    var gain = 1.0
    for(k <- 0 until stageCount) {
      var dgain = .0
      dgain = 1.0 + Math.pow(2.0, -2.0 * (k + 1))
      dgain = Math.sqrt(dgain)
      gain = gain * dgain
    }
    gain
  }

  def genAngleTable() : Vec[UInt] = {
    VecInit({(0 until stageCount).map { k =>
      var x = Math.atan2(1.0,Math.pow(2,k+1))
      x *= (4.0 * (1<<(phaseBits-2))) / (Math.PI * 2.0)
      Math.round(x).U(phaseBits.W)
    }})
  }


  val wx: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.S(workingBits.W))))
  val wy: Vec[SInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.S(workingBits.W))))
  val wph: Vec[UInt] = RegInit(VecInit(Seq.fill(stageCount+1)(0.U(phaseBits.W))))
  val aux: UInt = RegInit(0.U((stageCount+1).W))

  val ex: SInt = WireDefault(0.S(workingBits.W))
  val ey: SInt = WireDefault(0.S(workingBits.W))

  val pmg: SInt = WireDefault(0.S(workingBits.W))

  val angleTable: Vec[UInt] = WireDefault(genAngleTable())
  io.omg := pmg(workingBits-1,workingBits-magBits).asSInt()
  io.oph := wph(stageCount)
  io.oaux := aux(stageCount)

  when(io.ce) {
    ex := Cat(io.ix(inputBits-1).asSInt(), io.ix, 0.S((workingBits-inputBits-1).W)).asSInt()
    ey := Cat(io.iy(inputBits-1).asSInt(), io.iy, 0.S((workingBits-inputBits-1).W)).asSInt()
    pmg := wx(stageCount) + Cat(0.S(magBits.W), wx(stageCount)(workingBits-magBits).asSInt(), Fill(workingBits-magBits-1,!wx(stageCount)(workingBits-magBits)).asSInt()).asSInt()
    aux := aux(stageCount-1,0) ## io.iaux

    for(i <- 0 until stageCount) {
      if(i >=workingBits) {
        wx(i+1) := wx(i)
        wy(i+1) := wy(i)
        wph(i+1) := wph(i)
      } else {
        when(angleTable(i) === 0.U) {
          wx(i+1) := wx(i)
          wy(i+1) := wy(i)
          wph(i+1) := wph(i)
        }.elsewhen(wy(i)(workingBits-1) === 1.U) {
          wx(i+1) := wx(i) + (wy(i)>>(1+i))
          wy(i+1) := wy(i) - (wx(i)>>(1+i))
          wph(i+1) := wph(i) - angleTable(i)
        }.otherwise {
          wx(i+1) := wx(i) - (wy(i)>>(1+i))
          wy(i+1) := wy(i) + (wx(i)>>(1+i))
          wph(i+1) := wph(i) + angleTable(i)
        }
      }
    }

    switch(io.ix(inputBits-1) ## io.iy(inputBits-1)){
      is(0.U) {
        wx(0) := ex + ey
        wy(0) := ey - ex
        wph(0) := (1 << (phaseBits-3)).U
      }
      is(1.U) {
        wx(0) := ex - ey
        wy(0) := ex + ey
        wph(0) := (7 << (phaseBits-3)).U
      }
      is(2.U) {
        wx(0) := ey - ex
        wy(0) := -ex - ey
        wph(0) := (3 << (phaseBits-3)).U
      }
      is(3.U) {
        wx(0) := -ex - ey
        wy(0) := ex - ey
        wph(0) := (5 << (phaseBits-3)).U
      }
    }
  }.otherwise {
    aux := 0.U(stageCount.W)
  }
}
