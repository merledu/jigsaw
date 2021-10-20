package jigsaw.peripherals.spiflash

import caravan.bus.common.{AbstrRequest, AbstrResponse}
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, Decoupled, Fill, MuxCase, Enum}
import jigsaw.peripherals.spi._
// import jigsaw.peripherals.common._

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
    val ControlReg = RegInit(0.U(32.W))
    val TxDataReg    = RegInit(0.U(32.W))
    val TxDataValidReg = RegInit(0.B)
    val RxDataReg    = RegInit(0.U(32.W))
    val RxDataValidReg = RegInit(0.B)

    val vv = Mux(io.req.bits.addrRequest === 0.U, 0.B, 1.B)
    val maskedData = Wire(Vec(4, UInt(8.W)))
    maskedData := io.req.bits.activeByteLane.asTypeOf(Vec(4, Bool())) map (Fill(8,_))

    val setControlStatusReg :: readControlStatusReg :: setTxDataReg :: readTxDataReg :: readRxDataReg :: Nil = Enum(5)


    when (io.req.bits.addrRequest(3,0) === 0.U && io.req.bits.isWrite === 1.B){
        ControlReg := Mux(io.req.valid, io.req.bits.dataRequest & maskedData.asUInt, 0.U)

        io.rsp.bits.dataResponse := Mux(io.rsp.ready, io.req.bits.addrRequest, 0.U)
        io.rsp.bits.error := Mux(io.req.valid, 0.B, 1.B)
        

        List(io.req.ready, io.rsp.valid) map (_ := 1.B)
        // List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare)
    }
    .elsewhen(io.req.bits.addrRequest(3,0) === 0.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := Mux(io.rsp.ready, ControlReg, 0.U)
        io.rsp.bits.error := Mux(io.req.valid, 0.B, 1.B)
        

        List(io.req.ready, io.rsp.valid) map (_ := 1.B)
        // List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare) 
    }
    .elsewhen(io.req.bits.addrRequest(3,0) === 4.U && io.req.bits.isWrite === 1.B){
        when(ControlReg(3,2) === 0.U){ // READ
            TxDataReg := Mux(io.req.valid, Cat("b00000011".U,(io.req.bits.dataRequest & maskedData.asUInt)(23,0)), 0.U)
            TxDataValidReg := io.req.valid
        }
        .elsewhen(ControlReg(3,2) === 1.U){ // WR_EN
            TxDataReg := Mux(io.req.valid, Cat("b00000110".U, Fill(24,0.B)), 0.U)
            TxDataValidReg := io.req.valid
        }
        .elsewhen(ControlReg(3,2) === 2.U){ // PP
            TxDataReg := Mux(io.req.valid, Cat("b00000010".U,(io.req.bits.dataRequest & maskedData.asUInt)(23,0)), 0.U)
            TxDataValidReg := io.req.valid
        }
        .elsewhen(ControlReg(3,2) === 3.U){ // WR_DI
            TxDataReg := Mux(io.req.valid, Cat("b00000100".U, Fill(24,0.B)), 0.U)
            TxDataValidReg := io.req.valid
        }
        

        // TxDataReg := Mux(io.req.valid, Cat("b00000011".U,(io.req.bits.dataRequest & maskedData.asUInt)(23,0)), 0.U)
        // TxDataValidReg := io.req.valid

        io.rsp.bits.dataResponse := Mux(io.rsp.ready, io.req.bits.addrRequest, 0.U)
        io.rsp.bits.error := Mux(io.req.valid, 0.B, 1.B)

        List(io.req.ready, io.rsp.valid) map (_ := 1.B)
        // List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare)
    }
    .elsewhen(io.req.bits.addrRequest(3,0) === 4.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := Mux(io.rsp.ready, TxDataReg, 0.U)
        io.rsp.bits.error := Mux(io.req.valid, 0.B, 1.B)
        

        List(io.req.ready, io.rsp.valid) map (_ := 1.B)
        // List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare)
    }
    .elsewhen(io.req.bits.addrRequest(3,0) === 8.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := Mux(io.rsp.ready, RxDataReg, 0.U)
        io.rsp.bits.error := Mux(io.req.valid, 0.B, 1.B)
        io.rsp.valid := RxDataValidReg

        List(io.req.ready) map (_ := 1.B)
        // List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare)
    }
    .otherwise{
        List(io.req.ready, io.rsp.bits.error, io.rsp.valid) map (_ := 1.B)
        List(io.cs_n, io.sclk, io.mosi) map (_ := DontCare)
        io.rsp.bits.dataResponse := io.req.bits.addrRequest

        maskedData map (_ := DontCare)
    }


    val spiProtocol = Module(new Protocol())

    spiProtocol.io.data_in.bits  := TxDataReg
    // when(TxDataValidReg){
    //     spiProtocol.io.data_in.valid := 1.B
    //     TxDataValidReg := 0.B
    // }.otherwise{spiProtocol.io.data_in.valid := 0.B}
    spiProtocol.io.data_in.valid := TxDataValidReg
    spiProtocol.io.CPOL := ControlReg(1)
    spiProtocol.io.CPHA := ControlReg(0)
    spiProtocol.io.miso := io.miso
    spiProtocol.io.data_out.ready := 1.B
    List(io.mosi, io.sclk, io.cs_n) zip List(spiProtocol.io.mosi, spiProtocol.io.sck, spiProtocol.io.ss) map (a => a._1 := a._2)
    when(spiProtocol.io.data_out.valid){
        RxDataReg := spiProtocol.io.data_out.bits
        RxDataValidReg := 1.B
        }
















    // def counter(max: UInt) = {
    //     val x = RegInit(0.asUInt(max.getWidth.W))
    //     x := Mux(x === max, 0.U, x + 1.U)
    //     x
    // }
    // def pulse(n: UInt) = counter(n - 1.U) === 0.U
    // def toggle(p: Bool) = {
    //     val x = RegInit(false.B)
    //     x := Mux(p, !x, x)
    //     x
    // }
    // def clockGen(period: UInt) = toggle(pulse(period >> 1))

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
