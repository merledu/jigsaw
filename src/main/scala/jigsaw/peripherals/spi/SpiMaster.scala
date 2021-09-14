package jigsaw.peripherals.spi

import chisel3._
import chisel3.util._
import chisel3.experimental._
import jigsaw.peripherals.spi._

class SpiMasterIO(DW:Int) extends Bundle{
    val clk = Input(Clock())
    val rst_n = Input(Bool())

    val data_in = Input(UInt(DW.W))
    val start = Input(Bool())
    val spi_ready = Output(Bool())
    val data_out = Output(UInt(DW.W))
    val finish = Output(Bool())

    val cs_n = Output(Bool())
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())

}
class spi_master(implicit val spiConfig: Config) extends BlackBox(
    Map("CLK_FREQUENCE" -> spiConfig.CLK_FREQUENCE,
        "SPI_FREQUENCE" -> spiConfig.SPI_FREQUENCE,
        "DATA_WIDTH"    -> spiConfig.DW,
        "CPOL"          -> spiConfig.CPOL,
        "CPHA"          -> spiConfig.CPHA)) 
    with HasBlackBoxResource{

    val io = IO(new SpiMasterIO(spiConfig.DW))
    addResource("/spiResources/spi_master.v")
}

class SpiMaster(implicit val spiConfig: Config) extends Module{
    val io = IO(new SpiMasterIO(spiConfig.DW))
    val spi = Module(new spi_master)
    spi.io <> io

}