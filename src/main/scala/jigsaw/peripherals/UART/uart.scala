package jigsaw.peripherals.UART

import chisel3._
import chisel3.util._
import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse}
import jigsaw.peripherals.UART._

class uart(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends Module {
    val io = IO(new Bundle{
        val request = Flipped(Decoupled(req))   // req aaygi
        val response = Decoupled(rsp)           // resp jaayga
        val cio_uart_rx_i = Input(Bool())

        val cio_uart_tx_o = Output(Bool())
        val cio_uart_intr_tx_o = Output(Bool())
    })

    when (io.request.valid){
        val uart = Module (new UartTOP)
        uart.io.wdata := io.request.bits.dataRequest
        uart.io.addr := io.request.bits.addrRequest(7,0)
        uart.io.we := io.request.bits.isWrite
        uart.io.ren := ~io.request.bits.isWrite

        io.request.ready := 1.B

        //io.response.bits.dataResponse := uart.io.rdata
        //io.response.valid := 1.B

        io.response.bits.dataResponse := RegNext(Mux(io.response.ready , uart.io.rdata , 0.U))
        io.response.valid := RegNext(io.request.valid)

        //io.response.bits.error := uart.io.intr_tx

        io.response.bits.error := RegNext(Mux(io.response.ready , uart.io.intr_tx , 0.U))


        io.cio_uart_intr_tx_o := uart.io.intr_tx
        io.cio_uart_tx_o := uart.io.tx_o
        uart.io.rx_i := io.cio_uart_rx_i
    }.otherwise{
        io.request.ready := 1.B

        io.response.bits.dataResponse := 0.U
        io.response.bits.error := 0.B
        io.response.valid := 0.B

        io.cio_uart_intr_tx_o := DontCare
        io.cio_uart_tx_o := DontCare  
    }
}