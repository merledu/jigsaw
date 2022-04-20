package jigsaw.peripherals.i2c

import chisel3._
import chisel3.util._

class I2C_Top extends Module{
    val io = IO(new Bundle{
        val wdata = Input(UInt(32.W))
        val addr = Input(UInt(7.W))
        val ren = Input(Bool())
        val we = Input(Bool())
        // val sda_in = Input(Bool())

        val sda = Output(Bool())
        val scl = Output(Bool())
        val ready = Output(Bool())
        val stop = Output(Bool())
        val data_out = Output(UInt(8.W))
        val intr = Output(Bool())
    })

    //addresses
    val ADDR_Start_bit = 0.U
    val ADDR_SLAVE_ADDR = 4.U
    val ADDR_Read_Write_Bit = 8.U
    val ADDR_SDA_IN = 12.U              //send by slave
    val ADDR_DATA = 16.U
    // val ADDR_DATA_ACK = 20.U         //send by slave


    //registers
    val addr_start_bit = RegInit(0.B)
    val addr_slave_addr = RegInit(0.U(7.W))
    val addr_data = RegInit(0.U(8.W))
    val addr_read_write_bit = RegInit(0.B)
    // val addr_sda_in = RegInit(0.B)                 //send by slave               
    // // val addr_data_ack = RegInit(0.B)                //send by slave


    //assign values
    when(!(io.ren) && io.we){
    when(io.addr === ADDR_Start_bit){
        addr_start_bit := io.wdata(0)
    }.elsewhen(io.addr === ADDR_SLAVE_ADDR){
        addr_slave_addr := io.wdata(6,0)
    }.elsewhen(io.addr === ADDR_DATA){
        addr_data := io.wdata(7,0)
    }.elsewhen(io.addr === ADDR_Read_Write_Bit){
        addr_read_write_bit := io.wdata(0)
    // }.elsewhen(io.addr === ADDR_SDA_IN){          //send by slave
    //      addr_sda_in := io.wdata(0)
    // }.elsewhen(io.addr === ADDR_DATA_ACK){     //send by slave
    //      addr_data_ack := io.wdata(0)
    }.otherwise{
        addr_start_bit := 0.B
        addr_slave_addr := 0.U
        addr_data := 0.U
    }
    }


    //Module Connections
    val i2c_master = Module(new i2c_master)
    i2c_master.io.start := addr_start_bit
    i2c_master.io.addr := addr_slave_addr
    i2c_master.io.data := addr_data

    io.sda := i2c_master.io.i2c_sda
    io.scl := i2c_master.io.i2c_scl
    io.intr := i2c_master.io.i2c_intr
    io.ready := i2c_master.io.ready
    io.stop := i2c_master.io.stop


    val i2c_slave = Module(new i2c_slave)
    i2c_slave.io.sda_in := i2c_master.io.i2c_sda
    i2c_slave.io.ready := i2c_master.io.ready

    i2c_slave.io.scl_in := i2c_master.io.i2c_scl
    i2c_master.io.i2c_sda_in := i2c_slave.io.sda_out
    io.data_out := i2c_slave.io.data_out



}