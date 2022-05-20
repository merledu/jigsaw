package jigsaw
import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkDevice, TilelinkHost}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig, WishboneDevice, WishboneHost}
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.Decoupled
// import jigsaw.peripherals.spiflash.{Config,Spi}
import jigsaw.rams.sram._

class SramHarness(programFile:Option[String], val AW:Int = 10)(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new WBRequest()))
    val rsp = Decoupled(new WBResponse())
  })
  val hostAdapter = Module(new WishboneHost())
  val deviceAdapter = Module(new WishboneDevice())
  val sram = Module(new SRAM1kb(new WBRequest(), new WBResponse())(programFile, AW))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.wbMasterTransmitter <> deviceAdapter.io.wbMasterReceiver
  hostAdapter.io.wbSlaveReceiver <> deviceAdapter.io.wbSlaveTransmitter

  sram.io.req <> deviceAdapter.io.reqOut
  sram.io.rsp <> deviceAdapter.io.rspIn
}

object SramDriverWB extends App {
  implicit val config = WishboneConfig(32,32)
//   implicit val spiConfig = Config()
  (new ChiselStage).emitVerilog(new SramHarness(None))
}




class SramHarnessTL(programFile:Option[String], val AW:Int = 10)(implicit val config: TilelinkConfig ) extends Module {
  val io = IO(new Bundle {

    // bus interconnect interfaces
    val req = Flipped(Decoupled(new TLRequest()))
    val rsp = Decoupled(new TLResponse())

  })
  val hostAdapter = Module(new TilelinkHost())
  val deviceAdapter = Module(new TilelinkDevice())
  val sram = Module(new SRAM1kb(new TLRequest(), new TLResponse())(programFile, AW))

  hostAdapter.io.reqIn <> io.req
  io.rsp <> hostAdapter.io.rspOut
  hostAdapter.io.tlMasterTransmitter <> deviceAdapter.io.tlMasterReceiver
  hostAdapter.io.tlSlaveReceiver <> deviceAdapter.io.tlSlaveTransmitter

  sram.io.req <> deviceAdapter.io.reqOut
  sram.io.rsp <> deviceAdapter.io.rspIn

}

object SramDriverTL extends App {
  implicit val config = TilelinkConfig()
//   implicit val spiConfig = Config()
  (new ChiselStage).emitVerilog(new SramHarnessTL(None))
}
