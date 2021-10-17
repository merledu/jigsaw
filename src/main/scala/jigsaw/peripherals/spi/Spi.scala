package jigsaw.peripherals.spi

// import caravan.bus.common.{AddressMap, BusDecoder, DeviceAdapter, Switch1toN, DummyMemController, Peripherals} // imported DummyMemController
import caravan.bus.common.{AbstrRequest, AbstrResponse}
import caravan.bus.tilelink._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, Decoupled}
import chisel3.util.experimental.loadMemoryFromFile
import jigsaw.peripherals.spi._

class spiI_O[A <: AbstrRequest, B <: AbstrResponse]
          (gen: A, gen1: B) extends Bundle{

    // bus interconnect interfaces
    val req = Flipped(Decoupled(gen))
    val rsp = Decoupled(gen1)

    // master spi interfaces
    val cs_n = Output(Bool())
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())
}

class Spi[A <: AbstrRequest, B <: AbstrResponse]
          (gen: A, gen1: B)(implicit val spiConfig: Config) extends Module{

    val io = IO(new spiI_O(gen, gen1))

    val spiMaster = Module(new SpiWrapper())

    // bus interconnect IO bindings
    spiMaster.io.dataRequest.bits := io.req.bits.dataRequest
    spiMaster.io.dataRequest.valid := RegNext(io.req.valid)
    io.req.ready := spiMaster.io.dataRequest.ready

    spiMaster.io.addrRequest := io.req.bits.addrRequest

    spiMaster.io.isWrite := io.req.bits.isWrite
    spiMaster.io.activeByteLane := io.req.bits.activeByteLane

    io.rsp.bits.dataResponse :=spiMaster.io.dataResponse.bits
    io.rsp.valid := spiMaster.io.dataResponse.valid
    spiMaster.io.dataResponse.ready := io.rsp.ready

    io.rsp.bits.error := spiMaster.io.ackWrite



    // master spi IO bindings
    io.cs_n := spiMaster.io.cs_n
    io.sclk := spiMaster.io.sclk
    io.mosi := spiMaster.io.mosi
    
    spiMaster.io.miso := io.miso

}








// class Spi(implicit val config: TilelinkConfig, implicit val spiConfig: Config) extends Module {
//   val io = IO(new Bundle {
//     val valid = Input(Bool())
//     val addrReq = Input(UInt(spiConfig.DW.W))
//     val dataReq = Input(UInt(spiConfig.DW.W))
//     val byteLane = Input(UInt((spiConfig.DW/8).W))
//     val isWrite = Input(Bool())

//     val validResp = Output(Bool())
//     val dataResp = Output(UInt(spiConfig.DW.W))

//     //master spi
//     val cs_n = Output(Bool())
//     val sclk = Output(Bool())
//     val mosi = Output(Bool())
//     val miso = Input(Bool())

//     //slave spi
//     // val spi_slave_data_in = Input(UInt(32.W))
//     // val spi_slave_data_out = Output(UInt(32.W))
//     // val spi_slave_data_valid = Output(Bool())
//   })
// //   implicit val config = WishboneConfig(10, 32)
//   implicit val request = new TLRequest()    // implicit val for REQUEST
//   implicit val response = new TLResponse()  // implicit val for RESPONSE

//   val tlHost = Module(new TilelinkHost())
//   val tlSlave = Module(new TilelinkDevice())
//   val spiMaster = Module(new SpiWrapper())
//   // val spiSlave = Module(new SpiSlave())

//   tlHost.io.rspOut.ready := true.B  // IP always ready to accept data from tl host

//   tlHost.io.tlMasterTransmitter <> tlSlave.io.tlMasterReceiver
//   tlSlave.io.tlSlaveTransmitter <> tlHost.io.tlSlaveReceiver

//   tlHost.io.reqIn.valid := Mux(tlHost.io.reqIn.ready, io.valid, false.B)
//   tlHost.io.reqIn.bits.addrRequest := io.addrReq
//   tlHost.io.reqIn.bits.dataRequest := io.dataReq
//   tlHost.io.reqIn.bits.activeByteLane := io.byteLane
//   tlHost.io.reqIn.bits.isWrite := io.isWrite



// //   tlSlave.io.reqOut <>spiMasterio.req
// //   tlSlave.io.rspIn <>spiMasterio.rsp

//    spiMaster.io.dataRequest.bits := tlSlave.io.reqOut.bits.dataRequest
//    spiMaster.io.dataRequest.valid := RegNext(tlSlave.io.reqOut.valid)
//    tlSlave.io.reqOut.ready := spiMaster.io.dataRequest.ready

//    spiMaster.io.addrRequest := tlSlave.io.reqOut.bits.addrRequest

//    spiMaster.io.isWrite := tlSlave.io.reqOut.bits.isWrite
//    spiMaster.io.activeByteLane := tlSlave.io.reqOut.bits.activeByteLane

//     tlSlave.io.rspIn.bits.dataResponse :=spiMaster.io.dataResponse.bits
//     tlSlave.io.rspIn.valid := spiMaster.io.dataResponse.valid
//     spiMaster.io.dataResponse.ready := tlSlave.io.rspIn.ready

//     tlSlave.io.rspIn.bits.error := spiMaster.io.ackWrite


//     //master spi
//     io.cs_n :=spiMaster.io.cs_n
//     io.sclk :=spiMaster.io.sclk
//     io.mosi :=spiMaster.io.mosi
    
//     spiMaster.io.miso := io.miso


//     io.dataResp := tlHost.io.rspOut.bits.dataResponse
//     io.validResp := tlHost.io.rspOut.valid

//     // Slave ports
//   //   spiSlave.io.cs_n := spiMaster.io.cs_n
//   //   spiSlave.io.sclk := spiMaster.io.sclk
//   //   spiSlave.io.mosi := spiMaster.io.mosi
    
//   //  spiMaster.io.miso := spiSlave.io.miso

//   //   val clk_wire = WireInit(~clock.asUInt()(0))
//   //   val rst_wire = WireInit(~reset.asUInt()(0))

//   //   spiSlave.io.clk := clk_wire.asClock()
//   //   spiSlave.io.rst_n := rst_wire

//   //   spiSlave.io.data_in := io.spi_slave_data_in
//   //   io.spi_slave_data_out := spiSlave.io.data_out
//   //   io.spi_slave_data_valid := spiSlave.io.data_valid

// }