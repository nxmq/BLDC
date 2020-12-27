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
package bldc.bbox.ice40

import chisel3._
import chisel3.experimental._

class SB_SPI extends BlackBox(Map("BUS_ADDR74" -> "0b0000")) {
  var io = IO(new Bundle{
    val SBCLKI: Clock = Input(Clock())
    val SBRWI: Bool = Input(Bool())
    val SBSTBI: Bool = Input(Bool())
    val SBADRI7: Bool = Input(Bool())
    val SBADRI6: Bool = Input(Bool())
    val SBADRI5: Bool = Input(Bool())
    val SBADRI4: Bool = Input(Bool())
    val SBADRI3: Bool = Input(Bool())
    val SBADRI2: Bool = Input(Bool())
    val SBADRI1: Bool = Input(Bool())
    val SBADRI0: Bool = Input(Bool())
    val SBADRT7: Bool = Input(Bool())
    val SBADRT6: Bool = Input(Bool())
    val SBADRT5: Bool = Input(Bool())
    val SBADRT4: Bool = Input(Bool())
    val SBADRT3: Bool = Input(Bool())
    val SBADRT2: Bool = Input(Bool())
    val SBADRT1: Bool = Input(Bool())
    val SBADRT0: Bool = Input(Bool())
    val MI: Bool = Input(Bool())
    val SI: Bool = Input(Bool())
    val SCKI: Clock = Input(Clock())
    val SCSNI: Bool = Input(Bool())
    val SBDATO7: Bool = Output(Bool())
    val SBDATO6: Bool = Output(Bool())
    val SBDATO5: Bool = Output(Bool())
    val SBDATO4: Bool = Output(Bool())
    val SBDATO3: Bool = Output(Bool())
    val SBDATO2: Bool = Output(Bool())
    val SBDATO1: Bool = Output(Bool())
    val SBDATO0: Bool = Output(Bool())
    val SBACKO: Bool = Output(Bool())
    val SPIIRQ: Bool = Output(Bool())
    val SPIWKUP: Bool = Output(Bool())
    val SO: Bool = Output(Bool())
    val SOE: Bool = Output(Bool())
    val MO: Bool = Output(Bool())
    val MOE: Bool = Output(Bool())
    val SCKO: Clock = Output(Clock())
    val SCKOE: Bool = Output(Bool())
    val MCSNO3: Bool = Output(Bool())
    val MCSNO2: Bool = Output(Bool())
    val MCSNO1: Bool = Output(Bool())
    val MCSNO0: Bool = Output(Bool())
    val MCSNOE3: Bool = Output(Bool())
    val MCSNOE2: Bool = Output(Bool())
    val MCSNOE1: Bool = Output(Bool())
    val MCSNOE0: Bool = Output(Bool())
  })
}
