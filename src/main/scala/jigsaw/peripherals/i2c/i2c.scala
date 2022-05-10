package jigsaw.peripherals.i2c

import chisel3._
import chisel3.util._
import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse}

class i2c(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends Module {
    val io = IO(new Bundle{
        val request = Flipped(Decoupled(req))   // req aaygi
        val response = Decoupled(rsp)           // resp jaayga
        // val cio_i2c_sda_in = Input(Bool())

        val cio_i2c_sda = Output(Bool())
        val cio_i2c_scl = Output(Bool())
        val cio_i2c_intr = Output(Bool())
    })

    io.request.ready := 1.B

    val i2c_top = Module (new I2C_Top)

 
    val write_register, read_register  = Wire(Bool())
    val data_reg = Wire(UInt(32.W))
    val addr_reg = Wire(UInt(8.W))

    write_register := Mux(io.request.fire(), io.request.bits.isWrite, false.B)
    read_register := Mux(io.request.fire(), !io.request.bits.isWrite, false.B)
    data_reg := io.request.bits.dataRequest
    addr_reg := io.request.bits.addrRequest(6,0)
    i2c_top.io.wdata := data_reg
    i2c_top.io.addr := addr_reg
    i2c_top.io.we := write_register
    i2c_top.io.ren := read_register

    io.response.bits.dataResponse := RegNext(Mux(io.response.ready , i2c_top.io.data_out , 0.U))
    io.response.valid := RegNext(Mux(write_register || read_register, true.B, false.B))
    io.response.bits.error := RegNext(Mux(io.response.ready , i2c_top.io.intr , 0.U))

    // i2c_top.io.sda_in := io.cio_i2c_sda_in

    io.cio_i2c_sda := i2c_top.io.sda
    io.cio_i2c_scl := i2c_top.io.scl
    io.cio_i2c_intr := i2c_top.io.intr
}