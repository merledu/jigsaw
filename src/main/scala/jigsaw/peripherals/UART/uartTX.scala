package jigsaw.peripherals.UART

import chisel3._
import chisel3.util._


class uartTX extends Module{
    val io = IO(new Bundle{
        val tx_en = Input(Bool())
        val i_TX_Byte = Input(UInt(8.W))
        val CLKS_PER_BIT = Input(UInt(16.W))

        val o_TX_Serial = Output(Bool())
        val o_TX_Done = Output(Bool())


    })

    // val IDLE = 0.U
    // val TX_START_BIT = 1.U
    // val TX_DATA_BITS = 2.U
    // val TX_STOP_BIT = 3.U
    // val CLEANUP = 4.U

    

    val idle :: start :: data :: stop :: cleanup :: Nil = Enum(5)
    val r_SM_Main = RegInit(idle)
    val r_Clock_Count = RegInit(0.U(16.W))
    val r_Bit_Index = RegInit(0.U(3.W))
    val r_TX_Data = RegInit(0.U(8.W))
    val r_TX_Done = RegInit(0.B)

    //val IDLE :: TX_START_BIT :: TX_DATA_BITS :: TX_STOP_BIT :: CLEANUP :: Nil = Enum(5)
    

    io.o_TX_Serial := 0.B
    switch(r_SM_Main){
        is(idle){
            io.o_TX_Serial := 1.B
            r_TX_Done := 0.B
            r_Clock_Count := 0.U
            r_Bit_Index := 0.U

            when(io.tx_en === 1.B){
                r_TX_Data := io.i_TX_Byte
                r_SM_Main := start
            }.otherwise{
                r_SM_Main := idle
            }
        }

        is(start){
            io.o_TX_Serial := 0.B

            when(r_Clock_Count < io.CLKS_PER_BIT-1.U){
                r_Clock_Count := r_Clock_Count + 1.U
                r_SM_Main := start
            }.otherwise{
                r_Clock_Count := 0.U
                r_SM_Main := data
            }
        }

        is(data){
            io.o_TX_Serial := r_TX_Data(r_Bit_Index)

            when(r_Clock_Count < io.CLKS_PER_BIT - 1.U){
                r_Clock_Count := r_Clock_Count + 1.U
                r_SM_Main := data
            }.otherwise{
                r_Clock_Count := 0.U
                when(r_Bit_Index < 7.U){
                    r_Bit_Index := r_Bit_Index + 1.U
                    r_SM_Main := data
                }.otherwise{
                    r_Bit_Index := 0.U
                    r_SM_Main := stop
                }
            }
        }



        is(stop){
            io.o_TX_Serial := 1.B
            
            when(r_Clock_Count < io.CLKS_PER_BIT - 1.U){
                r_Clock_Count := r_Clock_Count + 1.U
                r_SM_Main := stop
            }.otherwise{
                r_TX_Done := 1.B
                r_Clock_Count := 0.U
                r_SM_Main := cleanup
            }

        }

        is(cleanup){
            r_TX_Done := 1.B
            r_SM_Main := idle
        }
    }

    

    io.o_TX_Done := r_TX_Done
}