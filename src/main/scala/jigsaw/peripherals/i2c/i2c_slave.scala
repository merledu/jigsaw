package jigsaw.peripherals.i2c

import chisel3._
import chisel3.util._



class i2c_slave extends Module{
    val io = IO(new Bundle{

        val sda_in = Input(Bool())
        val scl_in = Input(Bool())
        val ready = Input(Bool())

        val sda_out = Output(Bool())
        val data_out = Output(UInt(8.W))

    })






val rx_idle :: rx_addr  :: rx_WACK1 :: rx_output :: rx_stop :: Nil = Enum(5)

val slave_addr = 99.U //"b1111000".U
// val addr2 = 7.U //"b1100110".U
// val addr3 = 8.U //"b1110001".U
// val addr4 = 9.U //"b1010101".U

val count = RegInit(1.U(8.W))
val rx_state = RegInit(0.U(8.W))

rx_state := rx_idle

io.sda_out := 0.B


val addr_bit1 = RegInit(0.B)
val addr_bit2 = RegInit(0.B)
val addr_bit3 = RegInit(0.B)
val addr_bit4 = RegInit(0.B)
val addr_bit5 = RegInit(0.B)
val addr_bit6 = RegInit(0.B)
val addr_bit7 = RegInit(0.B)
val complete_addr = RegInit(0.U(7.W))

val data_bit1 = RegInit(0.B)
val data_bit2 = RegInit(0.B)
val data_bit3 = RegInit(0.B)
val data_bit4 = RegInit(0.B)
val data_bit5 = RegInit(0.B)
val data_bit6 = RegInit(0.B)
val data_bit7 = RegInit(0.B)
val data_bit8 = RegInit(0.B)
val data = RegInit(0.U(8.W))




switch(rx_state){
    is(rx_idle){
        when(io.ready){
            // data := 0.U
            rx_state := rx_addr
        }.otherwise{
            // data := 0.U
            rx_state := rx_idle
        }
    }

    is(rx_addr){
        when(count < 8.U){
            when(rx_state === 1.U && count === 1.U){
            addr_bit1 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_addr
           
        }

        when(count === 2.U){
            addr_bit2 := io.sda_in
        }.otherwise{
            count := count + 1.U
            rx_state := rx_addr
           
        }

        when(count === 3.U){
            addr_bit3 := io.sda_in
        }.otherwise{
            rx_state := rx_addr
            count := count + 1.U
           
        }

        when(count === 4.U){
            addr_bit4 := io.sda_in
        }.otherwise{
            rx_state := rx_addr
            count := count + 1.U
           
        }

        when(count === 5.U){
            addr_bit5 := io.sda_in
        }.otherwise{
            rx_state := rx_addr
            count := count + 1.U
           
        }

        when(count === 6.U){
            addr_bit6 := io.sda_in
        }.otherwise{
            rx_state := rx_addr
            count := count + 1.U
           
        }


        when(count === 7.U){
            addr_bit7 := io.sda_in
        }.otherwise{
            rx_state := rx_addr
            count := count + 1.U
           
        }
        count := count + 1.U

        }.otherwise{
            complete_addr := Cat(addr_bit1,addr_bit2,addr_bit3,addr_bit4,addr_bit5,addr_bit6,addr_bit7)
            data := 0.U
            rx_state := rx_WACK1

        }
        
    }

    is(rx_WACK1){
            when(complete_addr === slave_addr){
            io.sda_out := 0.B
            rx_state := rx_output
        }.otherwise{
            io.sda_out := 1.B
            rx_state := rx_stop
        }
        
        
    }

    is(rx_output){
        when(count < 16.U){

            when(rx_state === 3.U && count === 8.U){
            data_bit1 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 9.U){
            data_bit2 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 10.U){
            data_bit3 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 11.U){
            data_bit4 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }
        
        when(count === 12.U){
            data_bit5 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 13.U){
            data_bit6 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 14.U){
            data_bit7 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        when(count === 15.U){
            data_bit8 := io.sda_in
            
        }.otherwise{
            count := count + 1.U
            rx_state := rx_output
        }

        count := count + 1.U

        }.otherwise{
            data := Cat(data_bit1, data_bit2, data_bit3, data_bit4, data_bit5, data_bit6, data_bit7, data_bit8)
            io.sda_out := 0.U
            rx_state := rx_stop
        }
        
        
    }


    is(rx_stop){
        count := 0.U
        rx_state := rx_idle
    }

    


}

io.data_out := data

// dontTouch(shiftReg)
dontTouch(addr_bit1)
dontTouch(addr_bit2)
dontTouch(addr_bit3)
dontTouch(addr_bit4)
dontTouch(addr_bit5)
dontTouch(addr_bit6)
dontTouch(addr_bit7)
dontTouch(complete_addr)
dontTouch(data_bit1)
dontTouch(data_bit2)
dontTouch(data_bit3)
dontTouch(data_bit4)
dontTouch(data_bit5)
dontTouch(data_bit6)
dontTouch(data_bit7)
dontTouch(data_bit8)
dontTouch(data)

}


