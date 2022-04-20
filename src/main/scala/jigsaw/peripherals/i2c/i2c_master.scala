package jigsaw.peripherals.i2c

import chisel3._
import chisel3.util._



class i2c_master extends Module{
    val io = IO(new Bundle{
        val start = Input(Bool())
        val addr = Input(UInt(7.W))
        val data = Input(UInt(8.W))
        // val read_write = Input(Bool())
        val i2c_sda_in = Input(Bool())

        val i2c_sda = Output(Bool())
        val i2c_scl = Output(Bool())
        val ready = Output(Bool())
        val stop = Output(Bool())
        val i2c_intr = Output(Bool())

    })
11
  
    val idle_state :: start_state :: addr_state :: rw_state :: wack_state :: data_state :: wack2_state :: stop_state :: Nil = Enum(8)
    val state = RegInit(0.U(8.W))
    val count = RegInit(0.U(15.W))
    val saved_addr = RegInit(0.U(7.W))
    val saved_data = RegInit(0.U(8.W))
    val i2c_scl_enable = RegInit(1.B)
    val intr_done = RegInit(0.B)

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
                io.i2c_sda := saved_addr(count)
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
                io.i2c_sda := 0.U
                io.ready := 0.B
                io.stop := 0.B
                state := wack_state
                

            }

            is(wack_state){
                    io.i2c_sda := io.i2c_sda_in
                    when(io.i2c_sda_in === 0.B){
                        state :=  data_state
                        count := 7.U
                        io.ready := 0.B
                        io.stop := 0.B
                    }.otherwise{
                        state :=  stop_state
                        io.ready := 0.B
                        io.stop := 0.B
                    }
                

            }

            is(data_state){
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
                io.stop := 1.B
                state := idle_state
            }
        }
    }

    io.i2c_intr := intr_done

}


//------------------------------------------SLAVES----------------------------------------------


// val address = WireInit(0.U(7.W))
// val counter = RegInit(6.U(8.W))
// val rx_state = RegInit(0.U(8.W))



// val rx_idle :: rx_addr :: rx_rd_wr_enable :: rx_WACK1 :: rx_output :: rx_WACK2 :: rx_non_output :: rx_stop :: Nil = Enum(8)

// // val addr1 = 6.U //"b1111000".U
// // val addr2 = 7.U //"b1100110".U
// // val addr3 = 8.U //"b1110001".U
// // val addr4 = 9.U //"b1010101".U

// val address1 = RegInit(0.U(7.W))
// val reset1 = WireInit(0.U)
// val start1 = WireInit(0.U)

// rx_state := rx_idle

// start1 := io.start
// reset1 := rst

// address1 := address

// val output11 = RegInit(0.U)
// val output22 = RegInit(0.U)
// val output33 = RegInit(0.U)
// val output44 = RegInit(0.U)

// val addr11 = RegInit(0.U)
// dontTouch(addr11)

// io.output1 := output11
// io.output2 := output22
// io.output3 := output33
// io.output4 := output44

// switch(rx_state){
//     is(rx_idle){
//         when(io.ready){
//             counter := 6.U
//             output11 := 0.U
//             output22 := 0.U
//             output33 := 0.U
//             output44 := 0.U
//             rx_state := rx_addr
//         }.otherwise{
//             output11 := 0.U
//             output22 := 0.U
//             output33 := 0.U
//             output44 := 0.U
//             rx_state := rx_idle
//         }
//     }

//     is(rx_addr){
//         address1 := 6.U //"b1111000".U
//         output11 := 0.U
//         output22 := 0.U
//         output33 := 0.U
//         output44 := 0.U
//         rx_state := rx_rd_wr_enable
//     }

//     is(rx_rd_wr_enable){
//         output11 := 0.U
//         output22 := 0.U
//         output33 := 0.U
//         output44 := 0.U
//         rx_state := rx_WACK1

//     }

//     is(rx_WACK1){
//         when((address===addr1)||(address===addr2)||(address===addr3)||(address===addr4)){
//             WACK11 := 0.U
//             counter := 0.U
//             rx_state := rx_output
//         }.otherwise{
//             WACK11 := 1.U
//             counter := 0.U
//             rx_state := rx_non_output
//         }
//     }

//     is(rx_output){
//         when(address === addr1){
//             when(counter < 7.U){
//                 addr11 := io.i2c_sda
//             }.elsewhen(counter >= 7.U){
//                 output11 := io.i2c_sda
//                 WACK22 := 0.U
//             }
//             // output11 := io.i2c_sda
//             // WACK22 := 1.U
//             when(counter === 15.U){
//                 rx_state := rx_WACK2
//             }.otherwise{
//                 counter := counter + 1.U
//                 rx_state := rx_output
//             }
//         }.elsewhen(address === addr2){
//             output22 := io.i2c_sda
//             WACK22 := 0.U
//             when(counter === 0.U){
//                 rx_state := rx_WACK2
//             }.otherwise{
//                 counter := counter - 1.U
//                 rx_state := rx_output
//             }
//         }.elsewhen(address === addr3){
//             output33 := io.i2c_sda
//             WACK22 := 0.U
//             when(counter === 0.U){
//                 rx_state := rx_WACK2
//             }.otherwise{
//                 counter := counter - 1.U
//                 rx_state := rx_output
//             }
//         }.elsewhen(address === addr4){
//             output44 := io.i2c_sda
//             WACK22 := 0.U
//             when(counter === 0.U){
//                 rx_state := rx_WACK2
//             }.otherwise{
//                 counter := counter - 1.U
//                 rx_state := rx_output
//             }
//         }.otherwise{
//             output11 := 0.U
//             output22 := 0.U
//             output33 := 0.U
//             output44 := 0.U
//             WACK11 := 0.U
//             WACK22 := 0.U

//             when(counter === 0.U){
//                 rx_state := rx_WACK2
//             }.otherwise{
//                 counter := counter - 1.U
//             }
//         }
//     }

//     is(rx_non_output){
//         output11 := 0.U
//         output22 := 0.U
//         output33 := 0.U
//         output44 := 0.U

//         when(counter === 0.U){
//             rx_state := rx_WACK2
//         }.otherwise{
//             counter := counter - 1.U
//         }
//     }

//     is(rx_WACK2){
//         WACK22 := 1.U
//         output11 := 0.U
//         output22 := 0.U
//         output33 := 0.U
//         output44 := 0.U
//         rx_state := rx_stop

//     }

//     is(rx_stop){
//         output11 := 0.U
//         output22 := 0.U
//         output33 := 0.U
//         output44 := 0.U
//         WACK22 := 0.U
//         WACK11 := 0.U
//         rx_state := rx_idle
//     }


// }

// }







