package jigsaw.peripherals.i2c

import chisel3._
import chisel3.util._



class i2c_master extends Module{
    val io = IO(new Bundle{
        val start = Input(Bool())
        val addr = Input(UInt(7.W))
        val data = Input(UInt(8.W))
        val read_write = Input(Bool())
        val i2c_sda_in = Input(Bool())


        val i2c_sda = Output(Bool())
        val i2c_scl = Output(Bool())
        val ready = Output(Bool())
        val stop = Output(Bool())
        val i2c_intr = Output(Bool())

    })

  
    val idle_state :: start_state :: addr_state :: rw_state :: wack_state :: data_state :: wack2_state :: stop_state :: Nil = Enum(8)
    val state = RegInit(0.U(8.W))
    val count = RegInit(0.U(15.W))
    val saved_addr = RegInit(0.U(7.W))
    val saved_data = RegInit(0.U(8.W))
    val i2c_scl_enable = RegInit(1.B)
    val intr_done = RegInit(0.B)

    val WACK1 = WireInit(0.U) // Acknowledge for address
    val WACK2 = WireInit(0.U) //Acknowledge for data

    val WACK11 = RegInit(0.B)
    val WACK22 = RegInit(0.B)
    
    //val data_ack = 0.B

    state := idle_state

    io.i2c_sda := 1.B
    io.ready := 0.B
    io.stop := 0.B

    val clk = WireInit(clock.asUInt()(0))
    val rst = WireInit(reset.asUInt()(0))

    io.i2c_scl := Mux(i2c_scl_enable === 0.B , 1.B , ~clk)

    when(rst === 1.U){
        i2c_scl_enable := 0.B
    }.otherwise{
        when(( state === idle_state)||(state === start_state)||(state === stop_state)){
            i2c_scl_enable := 0.B
        }.otherwise{
             i2c_scl_enable := 1.B
        }
    }

    when(rst === 1.U){
        state := idle_state
        io.i2c_sda := 1.B
    }.otherwise{
        switch(state){
            is(idle_state){
                io.i2c_sda := 1.B
                intr_done := 0.B
                when(io.start === 1.B){
                    state := start_state
                    io.ready := 0.B
                    io.stop := 0.B
                }.otherwise{
                    state := idle_state
                }

            }

            is(start_state){
                io.i2c_sda := 0.B
                saved_addr := io.addr
                saved_data := io.data
                io.ready := 1.B
                io.stop := 0.B
                state := addr_state
                count := 6.U
            }

            is(addr_state){
                io.i2c_sda := io.addr(count)
                io.ready := 0.B
                io.stop := 0.B
                when(count === 0.U){
                    state := rw_state 
                }.otherwise{
                    count := count - 1.U
                    state := addr_state
                }

            }

            is(rw_state){
                when(io.read_write === 0.B){
                    io.i2c_sda := 0.B
                    io.ready := 0.B
                    io.stop := 0.B
                    state := wack_state
                    count := 8.U
                }.otherwise{
                    io.i2c_sda := 1.B
                    io.ready := 0.B
                    io.stop := 0.B
                    state := wack_state
                    count := 7.U
                }

            }

            is(wack_state){
                when(io.i2c_sda_in === 0.B){
                io.i2c_sda := io.i2c_sda_in
                io.ready := 0.B
                io.stop := 0.B
                state :=  data_state
                }.otherwise{
                    io.i2c_sda := io.i2c_sda_in
                    io.ready := 0.B
                    io.stop := 0.B
                    state :=  stop_state
                }

            }

            is(data_state){
                //io.i2c_sda := io.i2c_sda_in
                io.i2c_sda := io.data(count)
                io.ready := 0.B
                io.stop := 0.B
                when(count === 0.U){
                    state := wack2_state
                }.otherwise{
                    count := count - 1.U
                    state := data_state
                }

            }

            is(wack2_state){
                io.i2c_sda := io.i2c_sda_in
                io.ready := 0.B
                io.stop := 0.B
                state := stop_state
            }

            is(stop_state){
                io.i2c_sda := 1.B
                intr_done := 1.B
                io.ready := 0.B
                io.stop := 0.B
                state := idle_state
            }
        }
    }

    io.i2c_intr := intr_done



}







