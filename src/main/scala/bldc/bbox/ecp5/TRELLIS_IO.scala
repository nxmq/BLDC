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
package bldc.bbox.ecp5

import chisel3._
import chisel3.experimental.Analog

class TRELLIS_IO extends BlackBox(Map("DIR" -> "INPUT")) {
  var io = IO(new Bundle {
    val B = IO(Analog(1.W))
    val I = Input(Bool())
    val T = Input(Bool())
    val O = Output(Bool())
  })
}
