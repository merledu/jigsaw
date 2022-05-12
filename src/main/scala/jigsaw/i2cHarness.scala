package jigsaw

import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkDevice, TilelinkHost}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
import jigsaw.peripherals.i2c._

class i2cHarness(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())

    // I2C interfaces

    val i2c_sda = Output(Bool())
    val i2c_scl = Output(Bool())
    val i2c_intr = Output(Bool())
    // val i2c_sda_in = Input(Bool())

  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val i2c_wrapper = Module(new i2c(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  i2c_wrapper.io.request <> deviceAdapter.io.reqOut
  i2c_wrapper.io.response <> deviceAdapter.io.rspIn

    // i2c_wrapper.io.cio_uart_rx_i := io.cio_uart_rx_i
    io.i2c_sda := i2c_wrapper.io.cio_i2c_sda
    io.i2c_scl := i2c_wrapper.io.cio_i2c_scl
    io.i2c_intr := i2c_wrapper.io.cio_i2c_intr
}

object I2CHarnessDriver extends App {
  implicit val config = WishboneConfig(32,32)
  (new ChiselStage).emitVerilog(new i2cHarness())
}




class i2cHarness_TL(implicit val config: TilelinkConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new TLRequest()))
    val rsp = Decoupled(new TLResponse())

    // I2C interfaces

    val i2c_sda = Output(Bool())
    val i2c_scl = Output(Bool())
    val i2c_intr = Output(Bool())

  })
  val hostAdapter = Module(new TilelinkHost())
  val deviceAdapter = Module(new TilelinkDevice())
  val i2c_wrapper = Module(new i2c(new TLRequest(), new TLResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.tlMasterTransmitter <> deviceAdapter.io.tlMasterReceiver
  hostAdapter.io.tlSlaveReceiver <> deviceAdapter.io.tlSlaveTransmitter

  i2c_wrapper.io.request <> deviceAdapter.io.reqOut
  i2c_wrapper.io.response <> deviceAdapter.io.rspIn

    // i2c_wrapper.io.cio_uart_rx_i := io.cio_uart_rx_i
    io.i2c_sda := i2c_wrapper.io.cio_i2c_sda
    io.i2c_scl := i2c_wrapper.io.cio_i2c_scl
    io.i2c_intr := i2c_wrapper.io.cio_i2c_intr
}

object I2CHarnessDriverTL extends App {
  implicit val config = TilelinkConfig()
  (new ChiselStage).emitVerilog(new i2cHarness_TL())
}