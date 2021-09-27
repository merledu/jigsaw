package jigsaw.peripherals.spiflash

import caravan.bus.common.{AddressMap, BusDecoder, DeviceAdapter, Switch1toN, DummyMemController, Peripherals} // imported DummyMemController
import caravan.bus.common.{AbstrRequest, AbstrResponse}
// import caravan.bus.tilelink._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, Decoupled}
import jigsaw.peripherals.spi._

class Spi_IO[A <: AbstrRequest, B <: AbstrResponse]
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

    val io = IO(new Spi_IO(gen, gen1))

    // val spiMaster = Module(new SpiWrapper())

    // bus interconnect IO bindings
    // spiMaster.io.dataRequest.bits := io.req.bits.dataRequest
    // spiMaster.io.dataRequest.valid := RegNext(io.req.valid)
    // io.req.ready := spiMaster.io.dataRequest.ready

    // spiMaster.io.addrRequest := io.req.bits.addrRequest

    // spiMaster.io.isWrite := io.req.bits.isWrite
    // spiMaster.io.activeByteLane := io.req.bits.activeByteLane

    // io.rsp.bits.dataResponse :=spiMaster.io.dataResponse.bits
    // io.rsp.valid := spiMaster.io.dataResponse.valid
    // spiMaster.io.dataResponse.ready := io.rsp.ready

    // io.rsp.bits.error := spiMaster.io.ackWrite

    val spiProtocol = Module(new Protocol())

    spiProtocol.io.data_in.bits     := io.req.bits.addrRequest
    spiProtocol.io.data_in.valid    := io.req.valid
    io.req.ready                    := spiProtocol.io.data_in.ready 

    io.rsp.valid                    := spiProtocol.io.data_out.valid
    io.rsp.bits.dataResponse        := spiProtocol.io.data_out.bits
    io.rsp.bits.error               := 0.B
    spiProtocol.io.data_out.ready   := io.rsp.ready

    // master spi IO bindings
    io.cs_n := spiProtocol.io.ss
    io.sclk := spiProtocol.io.sck
    io.mosi := spiProtocol.io.mosi
    
    spiProtocol.io.miso := io.miso

}
