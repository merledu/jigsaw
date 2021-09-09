package jigsaw.peripherals.spi

import chisel3._
import chisel3.util._
import chisel3.experimental._

class SpiSlaveIO extends Bundle{
    val clk = Input(Clock())
    val rst_n = Input(Bool())

    val data_in = Input(UInt(32.W))
    // val start = Input(Bool())
    val data_out = Output(UInt(32.W))
    val data_valid = Output(Bool())
    

    val cs_n = Input(Bool())
    val sclk = Input(Bool())
    val mosi = Input(Bool())
    val miso = Output(Bool())

}
class SPI_Slave extends BlackBox with HasBlackBoxResource{
    val io = IO(new SpiSlaveIO)
    addResource("/spiResources/SPI_Slave.v")
}

class SpiSlave extends Module{
    val io = IO(new SpiSlaveIO)
    val spi = Module(new SPI_Slave)
    spi.io <> io

}