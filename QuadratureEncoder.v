module QuadratureEncoder(
  input         clock,
  input         reset,
  input         io_a,
  input         io_b,
  input         io_i,
  output [15:0] io_cnt,
  output        io_dir
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
`endif // RANDOMIZE_REG_INIT
  reg  aPrev; // @[QuadratureEncoder.scala 27:24]
  reg  aNew_1; // @[QuadratureEncoder.scala 28:28]
  reg  bPrev; // @[QuadratureEncoder.scala 29:24]
  reg  bNew_1; // @[QuadratureEncoder.scala 30:28]
  reg  iPrev; // @[QuadratureEncoder.scala 31:24]
  reg  iNew_1; // @[QuadratureEncoder.scala 32:28]
  reg [5:0] edgeDebounceCounter; // @[QuadratureEncoder.scala 33:38]
  reg [5:0] indexDebounceCounter; // @[QuadratureEncoder.scala 34:39]
  reg [15:0] position; // @[QuadratureEncoder.scala 35:27]
  reg  dir; // @[QuadratureEncoder.scala 36:22]
  wire  _T_2 = aNew_1 | bNew_1; // @[QuadratureEncoder.scala 42:28]
  wire  _T_3 = edgeDebounceCounter == 6'h32; // @[QuadratureEncoder.scala 44:35]
  wire [5:0] _T_5 = edgeDebounceCounter + 6'h1; // @[QuadratureEncoder.scala 48:48]
  wire [5:0] _T_9 = indexDebounceCounter + 6'h1; // @[QuadratureEncoder.scala 56:50]
  wire  _T_15 = bPrev ^ aPrev; // @[QuadratureEncoder.scala 62:18]
  wire [15:0] _T_19 = position + 16'h1; // @[QuadratureEncoder.scala 65:32]
  wire [15:0] _T_22 = position - 16'h1; // @[QuadratureEncoder.scala 71:30]
  assign io_cnt = position; // @[QuadratureEncoder.scala 38:10]
  assign io_dir = dir; // @[QuadratureEncoder.scala 37:10]
  always @(posedge clock) begin
    if (!(aNew_1 | bNew_1)) begin // @[QuadratureEncoder.scala 42:51]
      if (edgeDebounceCounter == 6'h32) begin // @[QuadratureEncoder.scala 44:61]
        aPrev <= aNew_1; // @[QuadratureEncoder.scala 45:11]
      end
    end
    aNew_1 <= io_a; // @[QuadratureEncoder.scala 40:18 QuadratureEncoder.scala 40:18]
    if (!(aNew_1 | bNew_1)) begin // @[QuadratureEncoder.scala 42:51]
      if (edgeDebounceCounter == 6'h32) begin // @[QuadratureEncoder.scala 44:61]
        bPrev <= bNew_1; // @[QuadratureEncoder.scala 46:11]
      end
    end
    bNew_1 <= io_b; // @[QuadratureEncoder.scala 41:18 QuadratureEncoder.scala 41:18]
    if (!(iNew_1)) begin // @[QuadratureEncoder.scala 51:27]
      if (indexDebounceCounter == 6'h32) begin // @[QuadratureEncoder.scala 53:62]
        iPrev <= iNew_1; // @[QuadratureEncoder.scala 54:11]
      end
    end
    iNew_1 <= io_i; // @[QuadratureEncoder.scala 50:18 QuadratureEncoder.scala 50:18]
    if (aNew_1 | bNew_1) begin // @[QuadratureEncoder.scala 42:51]
      edgeDebounceCounter <= 6'h0; // @[QuadratureEncoder.scala 43:25]
    end else if (!(edgeDebounceCounter == 6'h32)) begin // @[QuadratureEncoder.scala 44:61]
      edgeDebounceCounter <= _T_5; // @[QuadratureEncoder.scala 48:25]
    end
    if (iNew_1) begin // @[QuadratureEncoder.scala 51:27]
      indexDebounceCounter <= 6'h0; // @[QuadratureEncoder.scala 52:26]
    end else if (!(indexDebounceCounter == 6'h32)) begin // @[QuadratureEncoder.scala 53:62]
      indexDebounceCounter <= _T_9; // @[QuadratureEncoder.scala 56:26]
    end
    if (iPrev) begin // @[QuadratureEncoder.scala 59:15]
      position <= 16'h0; // @[QuadratureEncoder.scala 60:14]
    end else if (_T_3 & _T_2) begin // @[QuadratureEncoder.scala 61:107]
      if (_T_15) begin // @[QuadratureEncoder.scala 63:25]
        if (position < 16'h7fff) begin // @[QuadratureEncoder.scala 64:55]
          position <= _T_19; // @[QuadratureEncoder.scala 65:20]
        end else begin
          position <= 16'h0; // @[QuadratureEncoder.scala 67:18]
        end
      end else if (position > 16'h0) begin // @[QuadratureEncoder.scala 70:28]
        position <= _T_22; // @[QuadratureEncoder.scala 71:18]
      end else begin
        position <= 16'h7fff; // @[QuadratureEncoder.scala 73:18]
      end
    end
    if (!(iPrev)) begin // @[QuadratureEncoder.scala 59:15]
      if (_T_3 & _T_2) begin // @[QuadratureEncoder.scala 61:107]
        dir <= bPrev ^ aPrev; // @[QuadratureEncoder.scala 62:9]
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  aPrev = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  aNew_1 = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  bPrev = _RAND_2[0:0];
  _RAND_3 = {1{`RANDOM}};
  bNew_1 = _RAND_3[0:0];
  _RAND_4 = {1{`RANDOM}};
  iPrev = _RAND_4[0:0];
  _RAND_5 = {1{`RANDOM}};
  iNew_1 = _RAND_5[0:0];
  _RAND_6 = {1{`RANDOM}};
  edgeDebounceCounter = _RAND_6[5:0];
  _RAND_7 = {1{`RANDOM}};
  indexDebounceCounter = _RAND_7[5:0];
  _RAND_8 = {1{`RANDOM}};
  position = _RAND_8[15:0];
  _RAND_9 = {1{`RANDOM}};
  dir = _RAND_9[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
