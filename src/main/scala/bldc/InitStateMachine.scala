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

package bldc

import chisel3._
import chisel3.util._

class InitStateMachine extends Module {
  var io = new Bundle {
    val en : Bool = Input(Bool())
  }
//  //FSM States:
//  val fsmIdle :: fsmOffsetAlign :: fsmDirectionCheck
//              :: fsmRotarySensorPhaseCheck :: fsmCurrentSensorPhaseCheck
//              :: fsmRun :: fsmErrorStop :: Nil = Enum(3)
//
//  val fsmState : UInt = RegInit(fsmIdle)
//
//  when(io.en) {
//    switch(fsmState) {
//      is(fsmIdle) {
//
//      }
//      is(fsmOffsetAlign) {
//
//      }
//      is(fsmDirectionCheck) {
//
//      }
//      is(fsmRotarySensorPhaseCheck) {
//
//      }
//      is(fsmCurrentSensorPhaseCheck) {
//
//      }
//      is(fsmRun) {
//
//      }
//      is(fsmErrorStop) {
//
//      }
//    }
//  }



}
