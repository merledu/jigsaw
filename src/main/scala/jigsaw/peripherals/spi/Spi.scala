package jigsaw.peripherals.spi

import caravan.bus.common.{AddressMap, BusDecoder, DeviceAdapter, Switch1toN, DummyMemController, Peripherals} // imported DummyMemController
import caravan.bus.wishbone._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, Decoupled}
import chisel3.util.experimental.loadMemoryFromFile

class Spi/*(programFile: Option[String])*/(implicit val config: WishboneConfig) extends Module {
  val io = IO(new Bundle {
    val valid = Input(Bool())
    val addrReq = Input(UInt(config.addressWidth.W))
    val dataReq = Input(UInt(config.dataWidth.W))
    val byteLane = Input(UInt((config.dataWidth/config.granularity).W))
    val isWrite = Input(Bool())

    val validResp = Output(Bool())
    val dataResp = Output(UInt(32.W))

    //master spi
    // val cs_n = Output(Bool())
    // val sclk = Output(Bool())
    // val mosi = Output(Bool())
    // val miso = Input(Bool())

    //slave spi
    val spi_slave_data_in = Input(UInt(32.W))
    val spi_slave_data_out = Output(UInt(32.W))
    val spi_slave_data_valid = Output(Bool())
  })
//   implicit val config = WishboneConfig(10, 32)
  implicit val request = new WBRequest()    // implicit val for REQUEST
  implicit val response = new WBResponse()  // implicit val for RESPONSE

  val wbHost = Module(new WishboneHost())
  val wbSlave = Module(new WishboneDevice())
  val spiMaster = Module(new SpiWrapper())
  val spiSlave = Module(new SpiSlave())

  wbHost.io.rspOut.ready := true.B  // IP always ready to accept data from wb host

  wbHost.io.wbMasterTransmitter <> wbSlave.io.wbMasterReceiver
  wbSlave.io.wbSlaveTransmitter <> wbHost.io.wbSlaveReceiver

  wbHost.io.reqIn.valid := Mux(wbHost.io.reqIn.ready, io.valid, false.B)
  wbHost.io.reqIn.bits.addrRequest := io.addrReq
  wbHost.io.reqIn.bits.dataRequest := io.dataReq
  wbHost.io.reqIn.bits.activeByteLane := io.byteLane
  wbHost.io.reqIn.bits.isWrite := io.isWrite



//   wbSlave.io.reqOut <>spiMasterio.req
//   wbSlave.io.rspIn <>spiMasterio.rsp

   spiMaster.io.dataRequest.bits := wbSlave.io.reqOut.bits.dataRequest
   spiMaster.io.dataRequest.valid := wbSlave.io.reqOut.valid
   wbSlave.io.reqOut.ready :=spiMaster.io.dataRequest.ready

   spiMaster.io.addrRequest := wbSlave.io.reqOut.bits.addrRequest

   spiMaster.io.isWrite := wbSlave.io.reqOut.bits.isWrite
   spiMaster.io.activeByteLane := wbSlave.io.reqOut.bits.activeByteLane

    wbSlave.io.rspIn.bits.dataResponse :=spiMaster.io.dataResponse.bits
    wbSlave.io.rspIn.valid :=spiMaster.io.dataResponse.valid
   spiMaster.io.dataResponse.ready := wbSlave.io.rspIn.ready

    wbSlave.io.rspIn.bits.error :=spiMaster.io.ackWrite


    //master spi
    // io.cs_n :=spiMaster.io.cs_n
    // io.sclk :=spiMaster.io.sclk
    // io.mosi :=spiMaster.io.mosi
    
    //spiMaster.io.miso := io.miso


    io.dataResp := wbHost.io.rspOut.bits.dataResponse
    io.validResp := wbHost.io.rspOut.valid

    // Slave ports
    spiSlave.io.cs_n :=spiMaster.io.cs_n
    spiSlave.io.sclk :=spiMaster.io.sclk
    spiSlave.io.mosi :=spiMaster.io.mosi
    
   spiMaster.io.miso := spiSlave.io.miso

    val clk_wire = WireInit(~clock.asUInt()(0))
    val rst_wire = WireInit(~reset.asUInt()(0))

    spiSlave.io.clk := clk_wire.asClock()
    spiSlave.io.rst_n := rst_wire

    spiSlave.io.data_in := io.spi_slave_data_in
    io.spi_slave_data_out := spiSlave.io.data_out
    io.spi_slave_data_valid := spiSlave.io.data_valid

}