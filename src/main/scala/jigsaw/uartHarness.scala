package jigsaw

import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkDevice, TilelinkHost}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
import jigsaw.peripherals.UART._

class uartHarness(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())

    // UART interfaces

    val cio_uart_rx_i = Input(Bool())
    val cio_uart_tx_o = Output(Bool())
    val cio_uart_intr_tx_o = Output(Bool())

  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val uart_wrapper = Module(new Uart(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.masterTransmitter <> deviceAdapter.io.masterReceiver
  hostAdapter.io.slaveReceiver <> deviceAdapter.io.slaveTransmitter

  uart_wrapper.io.req <> deviceAdapter.io.reqOut
  uart_wrapper.io.rsp <> deviceAdapter.io.rspIn

    uart_wrapper.io.cio_uart_rx_i := io.cio_uart_rx_i
    io.cio_uart_tx_o := uart_wrapper.io.cio_uart_tx_o
    io.cio_uart_intr_tx_o := uart_wrapper.io.cio_uart_intr_tx_o
}

object UARTHarnessDriver extends App {
  implicit val config = WishboneConfig(32,32)
  (new ChiselStage).emitVerilog(new uartHarness())
}




class uartHarness_TL(implicit val config: TilelinkConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new TLRequest()))
    val rsp = Decoupled(new TLResponse())

    // UART interfaces

    val cio_uart_rx_i = Input(Bool())
    val cio_uart_tx_o = Output(Bool())
    val cio_uart_intr_tx_o = Output(Bool())

  })
  val hostAdapter = Module(new TilelinkHost())
  val deviceAdapter = Module(new TilelinkDevice())
  val uart_wrapper = Module(new Uart(new TLRequest(), new TLResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.masterTransmitter <> deviceAdapter.io.masterReceiver
  hostAdapter.io.slaveReceiver <> deviceAdapter.io.slaveTransmitter

  uart_wrapper.io.req <> deviceAdapter.io.reqOut
  uart_wrapper.io.rsp <> deviceAdapter.io.rspIn

    uart_wrapper.io.cio_uart_rx_i := io.cio_uart_rx_i
    io.cio_uart_tx_o := uart_wrapper.io.cio_uart_tx_o
    io.cio_uart_intr_tx_o := uart_wrapper.io.cio_uart_intr_tx_o
}

object UARTHarnessDriverTL extends App {
  implicit val config = TilelinkConfig()
  (new ChiselStage).emitVerilog(new uartHarness_TL())
}