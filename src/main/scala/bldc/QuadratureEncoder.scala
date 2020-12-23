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
import chisel3.util.log2Ceil

class QuadratureEncoder(counterSize: Int, edgeDebounceClocks: Int, indexDebounceClocks: Int) extends Module {
  val io = IO(new Bundle {
    val a: Bool = Input(Bool())
    val b: Bool = Input(Bool())
    val i: Bool = Input(Bool())
    val cnt: UInt = Output(UInt(counterSize.W))
    val dir: Bool = Output(Bool())
  })
  val aPrev: Bool = Reg(Bool())
  val aNew: Vec[Bool] = Reg(Vec(2,Bool()))
  val bPrev: Bool = Reg(Bool())
  val bNew: Vec[Bool] = Reg(Vec(2,Bool()))
  val iPrev: Bool = Reg(Bool())
  val iNew: Vec[Bool] = Reg(Vec(2,Bool()))
  val edgeDebounceCounter: UInt = Reg(UInt(log2Ceil(edgeDebounceClocks).W))
  val indexDebounceCounter: UInt = Reg(UInt(log2Ceil(indexDebounceClocks).W))
  val position: UInt = Reg(UInt(counterSize.W))
  val dir: Bool = Reg(Bool())
  io.dir := dir
  io.cnt := position

  aNew := VecInit(Array(aNew(0), io.a))
  bNew := VecInit(Array(bNew(0), io.b))
  when((aNew(0) ^ aNew(1)) | (bNew(0) ^ bNew(1))) {
    edgeDebounceCounter := 0.U
  }.elsewhen (edgeDebounceCounter === edgeDebounceClocks.U) {
    aPrev := aNew(1)
    bPrev := bNew(1)
  }.otherwise {
    edgeDebounceCounter := edgeDebounceCounter + 1.U
  }
  iNew := VecInit(Array(iNew(0), io.i))
  when(iNew(0) ^ iNew(1)) {
    indexDebounceCounter := 0.U
  }.elsewhen(indexDebounceCounter === indexDebounceClocks.U) {
    iPrev := iNew(1)
  }.otherwise {
    indexDebounceCounter := indexDebounceCounter + 1.U
  }

  when(iPrev) {
    position := 0.U
  }.elsewhen(edgeDebounceCounter === edgeDebounceClocks.U && ((aNew(0) ^ aNew(1)) | (bNew(0) ^ bNew(1)))) {
    dir := bPrev ^ aPrev
    when(bPrev ^ aPrev) {
      when(position < ((1 << (counterSize-1)) - 1).U) {
          position := position + 1.U
      }.otherwise {
        position := 0.U
      }
    }.otherwise {
      when(position > 0.U) {
        position := position - 1.U
      }.otherwise {
        position := ((1 << (counterSize-1)) - 1).U
      }
    }
  }
}
