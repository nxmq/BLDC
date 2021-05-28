//SPDX-License-Identifier: 0BSD
//Copyright (c) 2020-2021 Nicolas Machado
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

class Mux4(w: Int) extends Module {
  val io = IO(new Bundle {
    val sel = Input(UInt(2.W))
    val in0 = Input(UInt(w.W))
    val in1 = Input(UInt(w.W))
    val in2 = Input(UInt(w.W))
    val in3 = Input(UInt(w.W))
    val out = Output(UInt(w.W))
  })

  io.out := Mux(io.sel(1),
    Mux(io.sel(0), io.in0, io.in1),
    Mux(io.sel(0), io.in2, io.in3))
}


object Mux4 {
  def apply(sel: UInt, in0: UInt, in1: UInt, in2: UInt, in3: UInt): UInt = {
    val m = Module(new Mux4(in0.getWidth))
    m.io.sel := sel
    m.io.in0 := in0
    m.io.in1 := in1
    m.io.in2 := in2
    m.io.in3 := in3
    m.io.out
  }
}