package jigsaw.peripherals.UART

import chisel3._
import chisel3.util._
import chisel3.util.HasBlackBoxResource


class uartcoreIO extends Bundle{
    val clk_i = Input(Bool())
    val rst_ni = Input(Bool())
    val ren = Input(Bool())
    val we = Input(Bool())
    val wdata = Input(UInt(32.W))
    val addr = Input(UInt(8.W))
    val rx_i = Input(Bool())

    val rdata = Output(UInt(32.W))
    val tx_o = Output(Bool())
    val intr_tx = Output(Bool())
}

class uart_core extends BlackBox with HasBlackBoxResource{
  val io = IO(new uartcoreIO)
    addResource("/uart_core.v")

}

class UART_CORE extends Module{
  val io = IO(new Bundle{
    val ren = Input(Bool())
    val we = Input(Bool())
    val wdata = Input(UInt(32.W))
    val addr = Input(UInt(8.W))
    val rx_i = Input(Bool())

    val rdata = Output(UInt(32.W))
    val tx_o = Output(Bool())
    val intr_tx = Output(Bool())
  })

  val u_core = Module(new uart_core)
  
  val clk = WireInit(clock.asUInt()(0))
  val rst = WireInit(reset.asUInt()(0))

  u_core.io.clk_i := ~clk
  u_core.io.rst_ni := ~rst

  // u_core.io.rst_ni := io.rst_ni
  u_core.io.ren := io.ren
  u_core.io.we := io.we
  u_core.io.wdata := io.wdata
  u_core.io.addr := io.addr
  u_core.io.rx_i := io.rx_i

  io.rdata := u_core.io.rdata
  io.tx_o := u_core.io.tx_o
  io.intr_tx := u_core.io.intr_tx

}