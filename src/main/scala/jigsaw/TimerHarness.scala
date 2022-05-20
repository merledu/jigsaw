package jigsaw
import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkDevice, TilelinkHost}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
// import jigsaw.peripherals.spiflash.{Config,Spi}
// import jigsaw.rams.sram._
import jigsaw.peripherals.timer._

class TimerHarness(implicit val config: WishboneConfig ) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())

    val cio_timer_intr_cmp = Output(Bool())
    val cio_timer_intr_ovf = Output(Bool())
  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val timer = Module(new Timer(new WBRequest(), new WBResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  timer.io.req <> deviceAdapter.io.reqOut
  timer.io.rsp <> deviceAdapter.io.rspIn

  io.cio_timer_intr_cmp := timer.io.cio_timer_intr_cmp
  io.cio_timer_intr_ovf := timer.io.cio_timer_intr_ovf
}

object TimerDriverWB extends App {
  implicit val config = WishboneConfig(32,32)
//   implicit val spiConfig = Config()
  (new ChiselStage).emitVerilog(new TimerHarness())
}




class TimerHarnessTL(implicit val config: TilelinkConfig ) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new TLRequest()))
    val rsp = Decoupled(new TLResponse())

    val cio_timer_intr_cmp = Output(Bool())
    val cio_timer_intr_ovf = Output(Bool())
    
  })
  val hostAdapter = Module(new TilelinkHost())
  val deviceAdapter = Module(new TilelinkDevice())
  val timer = Module(new Timer(new TLRequest(), new TLResponse()))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.tlMasterTransmitter <> deviceAdapter.io.tlMasterReceiver
  hostAdapter.io.tlSlaveReceiver <> deviceAdapter.io.tlSlaveTransmitter

  timer.io.req <> deviceAdapter.io.reqOut
  timer.io.rsp <> deviceAdapter.io.rspIn

  io.cio_timer_intr_cmp := timer.io.cio_timer_intr_cmp
  io.cio_timer_intr_ovf := timer.io.cio_timer_intr_ovf

}

object TimerDriverTL extends App {
  implicit val config = TilelinkConfig()
//   implicit val spiConfig = Config()
  (new ChiselStage).emitVerilog(new TimerHarnessTL())
}
