package jigsaw.peripherals.spiflash

import caravan.bus.common.{AddressMap, BusDecoder, DeviceAdapter, Switch1toN, DummyMemController, Peripherals} // imported DummyMemController
import caravan.bus.common.{AbstrRequest, AbstrResponse}
// import caravan.bus.tilelink._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, Decoupled}
import jigsaw.peripherals.spi._
import jigsaw.peripherals.common._

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
    // val spiProtocol = Module(new Protocol())
    val ControlReg = RegInit(0.U(32.W))

    val vv = Mux(io.req.bits.addrRequest === 0.U, 0.B, 1.B)

    when (io.req.bits.addrRequest === 0.U && io.req.bits.isWrite === 1.B){
        ControlReg := io.req.bits.dataRequest

        io.req.ready := 1.B
        io.rsp.bits.dataResponse := io.req.bits.dataRequest
        io.rsp.bits.error := 0.B
        io.rsp.valid := 1.B

        io.cs_n := DontCare
        io.sclk := DontCare
        io.mosi := DontCare
    }.elsewhen(io.req.bits.addrRequest === 3.U && io.req.bits.isWrite === 1.B){
        val spiProtocol = Module(new Protocol())

        spiProtocol.io.data_in.bits  := io.req.bits.dataRequest
        spiProtocol.io.data_in.valid := vv
        io.req.ready := spiProtocol.io.data_in.ready

        io.rsp.bits.dataResponse := spiProtocol.io.data_out.bits
        io.rsp.valid := spiProtocol.io.data_out.valid
        spiProtocol.io.data_out.ready := io.rsp.ready
        io.rsp.bits.error := 0.B

        spiProtocol.io.resetProtocol := 0.B
        spiProtocol.io.CPOL := ControlReg(1)
        spiProtocol.io.CPHA := ControlReg(0)
        spiProtocol.io.miso := io.miso

        io.mosi := spiProtocol.io.mosi
        io.sclk := spiProtocol.io.sck
        io.cs_n := spiProtocol.io.ss

    }.otherwise{
        io.req.ready := 1.B
        io.rsp.bits.dataResponse := io.req.bits.addrRequest
        io.rsp.bits.error := 1.B
        io.rsp.valid := 1.B

        io.cs_n := DontCare
        io.sclk := DontCare
        io.mosi := DontCare
    }
}






































    // when(io.req.bits.addrRequest === 3.U & io.req.bits.isWrite === 1.B){
    //     val spiProtocol = Module(new Protocol())
    //     spiProtocol.io.CPOL := ControlReg(1)
    //     spiProtocol.io.CPHA := ControlReg(0)

    //     spiProtocol.io.data_in.bits     := io.req.bits.dataRequest
    //     spiProtocol.io.data_in.valid    := io.req.valid
    //     io.req.ready                    := spiProtocol.io.data_in.ready 

    //     io.rsp.valid                    := spiProtocol.io.data_out.valid
    //     io.rsp.bits.dataResponse        := spiProtocol.io.data_out.bits
    //     io.rsp.bits.error               := 0.B
    //     spiProtocol.io.data_out.ready   := io.rsp.ready

    //     io.cs_n := spiProtocol.io.ss
    //     io.sclk := spiProtocol.io.sck
    //     io.mosi := spiProtocol.io.mosi
        
    //     spiProtocol.io.miso := io.miso

    //     spiProtocol.io.resetProtocol := 0.B
    //     }
    // .elsewhen(io.req.bits.addrRequest === 0.U & io.req.bits.isWrite === 1.B){
        // val spiProtocol = Module(new Protocol())
        // ControlReg := io.req.bits.dataRequest
        
        // spiProtocol.io.config           := io.req.bits.dataRequest

        // spiProtocol.io.data_in.bits     := DontCare
        // spiProtocol.io.data_in.valid    := DontCare
        // io.req.ready                    := spiProtocol.io.data_in.ready
        // io.req.ready                       := 1.B

        // io.rsp.valid                    := 1.B
        // io.rsp.bits.dataResponse        := io.req.bits.dataRequest
        // io.rsp.bits.error               := 0.B
        // spiProtocol.io.data_out.ready   := io.rsp.ready

        // io.cs_n := DontCare
        // io.sclk := DontCare
        // io.mosi := DontCare
        
        // spiProtocol.io.miso := io.miso

        // spiProtocol.io.CPOL := DontCare
        // spiProtocol.io.CPHA := DontCare

        // spiProtocol.io.resetProtocol := 1.B

    // }
    // .elsewhen(io.req.bits.addrRequest(31,24) === 2.U & io.req.bits.isWrite === 1.B){}
    // .otherwise{
        // spiProtocol.io.config           := DontCare
        // spiProtocol.io.data_in.bits     := DontCare
        // spiProtocol.io.data_in.valid    := DontCare
        // spiProtocol.io.data_out.ready   := DontCare
        // spiProtocol.io.miso             := io.miso

        // io.rsp.valid                    := 1.B
        // io.rsp.bits.dataResponse        := io.req.bits.addrRequest  // error on this address
        // io.rsp.bits.error               := 1.B

        // io.req.ready := 1.B
        
        // io.cs_n := 1.B
        // io.sclk := DontCare
        // io.mosi := DontCare
        
        // spiProtocol.io.CPOL := DontCare
        // spiProtocol.io.CPHA := DontCare

        // spiProtocol.io.miso := io.miso

        // spiProtocol.io.resetProtocol := 1.B
    // }



    // when(io.req.bits.addrRequest === 0.B & io.req.bits.isWrite === 1.B){
    //     spiProtocol.io.config           := Mux(io.req.valid,io.req.bits.dataRequest,DontCare)
    //     spiProtocol.io.data_in.bits     := DontCare
    //     spiProtocol.io.data_in.valid    := 0.B
    //     io.req.ready                    := spiProtocol.io.data_in.ready 

    //     io.rsp.valid                    := 1.B
    //     io.rsp.bits.dataResponse        := 0.U
    //     io.rsp.bits.error               := 0.B
    //     spiProtocol.io.data_out.ready   := 1.B
    // }
    // .otherwise{
//////////////////////////////////
        // spiProtocol.io.config := 0.U

        // spiProtocol.io.data_in.bits     := io.req.bits.addrRequest
        // spiProtocol.io.data_in.valid    := io.req.valid
        // io.req.ready                    := spiProtocol.io.data_in.ready 

        // io.rsp.valid                    := spiProtocol.io.data_out.valid
        // io.rsp.bits.dataResponse        := spiProtocol.io.data_out.bits
        // io.rsp.bits.error               := 0.B
        // spiProtocol.io.data_out.ready   := io.rsp.ready
//////////////////////////////////
    // }
    // master spi IO bindings
    // io.cs_n := spiProtocol.io.ss
    // io.sclk := spiProtocol.io.sck
    // io.mosi := spiProtocol.io.mosi
    
    // spiProtocol.io.miso := io.miso

// }
