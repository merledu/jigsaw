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
    val request = Flipped(Decoupled(new WBRequest()))
    val response = Decoupled(new WBResponse())

    val i2c_sda_in = Input(Bool())

    // I2C interfaces

    val i2c_sda = Output(Bool())
    val i2c_scl = Output(Bool())
    val i2c_intr = Output(Bool())

  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val i2c_wrapper = Module(new i2c(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.request
  io.response <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  i2c_wrapper.io.request <> deviceAdapter.io.reqOut
  i2c_wrapper.io.response <> deviceAdapter.io.rspIn

    // i2c_wrapper.io.cio_i2c_sda_in := io.i2c_sda_in
    io.i2c_sda := i2c_wrapper.io.cio_i2c_sda
    io.i2c_scl := i2c_wrapper.io.cio_i2c_scl
    io.i2c_intr := i2c_wrapper.io.cio_i2c_intr
}
