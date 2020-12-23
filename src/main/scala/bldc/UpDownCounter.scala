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

class UpDownCounter(n: Int, min: Int = 0, maxv: Option[Int] = None) extends Module {
  val io = IO(new Bundle {
    val cnt: UInt = Output(UInt(n.W))
    val en: Bool = Input(Bool())
    val dir: Bool = Output(Bool())
  })
  val dir: Bool = RegInit(true.B)
  val cnt: UInt = RegInit(min.U(n.W))
  val max: Int = maxv.getOrElse((1 << n) - 1)
  when(io.en) {
    val next_cnt = Mux(dir, cnt + 1.U, cnt - 1.U)
    cnt := next_cnt
    when (next_cnt === min.U || next_cnt === max.U) {
      dir := !dir
    }
  }
  io.cnt := cnt
  io.dir := dir
}
