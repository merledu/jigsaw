package jigsaw

import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkDevice, TilelinkHost}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
import jigsaw.peripherals.gpio._

class gpioHarness(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())

    // UART interfaces

    val cio_gpio_i = Input(UInt(32.W))
    val cio_gpio_o = Output(UInt(32.W))
    val cio_gpio_en_o = Output(UInt(32.W))
    val intr_gpio_o = Output(UInt(32.W))

  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val gpio_wrapper = Module(new Gpio(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  gpio_wrapper.io.req <> deviceAdapter.io.reqOut
  gpio_wrapper.io.rsp <> deviceAdapter.io.rspIn

    gpio_wrapper.io.cio_gpio_i := io.cio_gpio_i
    io.cio_gpio_o := gpio_wrapper.io.cio_gpio_o
    io.cio_gpio_en_o := gpio_wrapper.io.cio_gpio_en_o
    io.intr_gpio_o := gpio_wrapper.io.intr_gpio_o
}

object GPIOHarnessDriver extends App {
  implicit val config = WishboneConfig(32,32)
  (new ChiselStage).emitVerilog(new gpioHarness())
}




class gpioHarness_TL(implicit val config: TilelinkConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new TLRequest()))
    val rsp = Decoupled(new TLResponse())

    // UART interfaces

    val cio_gpio_i = Input(UInt(32.W))
    val cio_gpio_o = Output(UInt(32.W))
    val cio_gpio_en_o = Output(UInt(32.W))
    val intr_gpio_o = Output(UInt(32.W))


  })
  val hostAdapter = Module(new TilelinkHost())
  val deviceAdapter = Module(new TilelinkDevice())
  val gpio_wrapper = Module(new Gpio(new TLRequest(), new TLResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.tlMasterTransmitter <> deviceAdapter.io.tlMasterReceiver
  hostAdapter.io.tlSlaveReceiver <> deviceAdapter.io.tlSlaveTransmitter

  gpio_wrapper.io.req <> deviceAdapter.io.reqOut
  gpio_wrapper.io.rsp <> deviceAdapter.io.rspIn

    gpio_wrapper.io.cio_gpio_i := io.cio_gpio_i
    io.cio_gpio_o := gpio_wrapper.io.cio_gpio_o
    io.cio_gpio_en_o := gpio_wrapper.io.cio_gpio_en_o
    io.intr_gpio_o := gpio_wrapper.io.intr_gpio_o
}

object GPIOHarnessDriverTL extends App {
  implicit val config = TilelinkConfig()
  (new ChiselStage).emitVerilog(new gpioHarness_TL())
}