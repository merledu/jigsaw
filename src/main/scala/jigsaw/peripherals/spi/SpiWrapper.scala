package jigsaw.peripherals.spi

import chisel3._
import chisel3.util._
import chisel3.experimental._
import jigsaw.peripherals.spi._

class WrapperIO(DW:Int) extends Bundle{
    val dataRequest = Flipped(Decoupled(UInt(DW.W)))
    val addrRequest = Input(UInt(DW.W))
    val activeByteLane = Input(UInt((DW/8).W))
    val isWrite = Input(Bool())

    val dataResponse = Decoupled(UInt(32.W))
    val ackWrite = Output(Bool())

    // val clk = Input(Clock())
    // val rst_n = Input(Bool())

    val cs_n = Output(Bool())
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())
}

class SpiWrapper(implicit val spiConfig: Config) extends Module{
    val io = IO(new WrapperIO(spiConfig.DW))
    val spiMaster = Module(new SpiMaster)

    // Bus Interface
    spiMaster.io.data_in := io.dataRequest.bits// & io.activeByteLane
    spiMaster.io.start := RegNext(io.dataRequest.valid)
    io.dataRequest.ready := spiMaster.io.spi_ready

    // TODO: addrRequest and isWrite 

    io.dataResponse.bits := Mux(io.dataResponse.ready, spiMaster.io.data_out, DontCare)
    io.dataResponse.valid := spiMaster.io.finish

    // io.dataResponse.bits := Mux(~io.dataResponse.ready & ~spiMaster.io.finish, spiMaster.io.data_out, DontCare)
    // io.dataResponse.valid := spiMaster.io.finish

    // TODO: ackWrite
    io.ackWrite := DontCare

    // SPI Pins
    val clk_wire = WireInit(~clock.asUInt()(0))
    val rst_wire = WireInit(~reset.asUInt()(0))

    spiMaster.io.clk := clk_wire.asClock()
    spiMaster.io.rst_n := rst_wire

    io.cs_n := spiMaster.io.cs_n
    io.sclk := spiMaster.io.sclk
    io.mosi := spiMaster.io.mosi
    
    spiMaster.io.miso := io.miso

}
