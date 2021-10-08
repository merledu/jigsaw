package jigsaw.peripherals.UART

import chisel3._
import chisel3.util._
import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse}
import jigsaw.peripherals.UART.UART_CORE

class UARTWrapper(val req:AbstrRequest, val rsp:AbstrResponse)(implicit val config:BusConfig) extends Module {
    val io = IO(new Bundle{
        val request = Flipped(Decoupled(req))   // req aaygi
        val response = Decoupled(rsp)           // resp jaayga
        val cio_uart_rx_i = Input(Bool())

        val cio_uart_tx_o = Output(Bool())
        val cio_uart_intr_tx_o = Output(Bool())
    })

    // io.request.ready := true.B

    // val uart = Module (new UART_CORE)
    when (io.request.valid){
        val uart = Module (new UART_CORE)
        uart.io.wdata := io.request.bits.dataRequest
        uart.io.addr := io.request.bits.addrRequest(7,0)
        uart.io.we := io.request.bits.isWrite
        uart.io.ren := ~io.request.bits.isWrite

        io.request.ready := 1.B

        io.response.bits.dataResponse := uart.io.rdata
        io.response.valid := 1.B

        io.response.bits.error := uart.io.intr_tx

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


    // val data = Wire(Vec(4, UInt()))

    // for sending request
    // when(io.request.fire() & io.request.bits.isWrite){                                       // if req is of write
    //     val maskedData = io.request.bits.dataRequest.asTypeOf(Vec(4, UInt(8.W)))            // breaking into Vecs to apply masking
    //     data := io.request.bits.activeByteLane.asBools zi+p maskedData map {                // applying maskiing a/c to mask bits (activeByteLane)
    //         case (b:Bool, i:UInt) => Mux(b, i, 0.U)
    //     }

    //     // feed these pins into the BLACK BOX of SRAM/Peripheral
    //     // data.asUInt                     // ye data hy 
    //     // io.request.bits.addrRequest     // ye address hy
    //     // io.request.bits.isWrite         // ye write enable h

    //     usart.io.wdata := data.asUInt
    //     usart.io.addr := io.request.bits.addrRequest
    //     usart.io.we := io.request.bits.isWrite
    //     usart.io.ren := !io.request.bits.isWrite

    //     io.cio_uart_tx_o := usart.io.tx_o
    //     io.cio_uart_intr_tx_o := usart.io.intr_tx
    //     usart.io.rx_i := io.cio_uart_rx_i



    // }.elsewhen(io.request.fire() & !io.request.bits.isWrite){                                // if req is of read
        
    //     // io.request.bits.addrRequest     // ye address hy
    //     // io.request.bits.dataRequest     // ye data hy, but kisi kaam ka nahi
    //     // io.request.bits.isWrite         // ye write enable h, low hga read k lye

    //     usart.io.addr := io.request.bits.addrRequest
    //     usart.io.wdata := io.request.bits.dataRequest
    //     usart.io.we := io.request.bits.isWrite
    //     usart.io.ren := io.request.bits.isWrite

    //     io.cio_uart_tx_o := usart.io.tx_o
    //     io.cio_uart_intr_tx_o := usart.io.intr_tx
    //     usart.io.rx_i := io.cio_uart_rx_i

    // }.otherwise {
    //     // DontCares feed krdena, if needed

    //     usart.io.addr := DontCare
    //     usart.io.wdata := DontCare
    //     usart.io.we := DontCare
    //     usart.io.ren := DontCare

    //     io.cio_uart_tx_o := DontCare
    //     io.cio_uart_intr_tx_o := DontCare
    //     usart.io.rx_i := DontCare
    // }

    // // For recieveing response
    // val responseData = usart.io.wdata                 // yahan data phek do, response se any wala

    // // CAUTION => If data is coming after 1 or more cycles, you have preserve the io.request.bits.activeByteLane pin
    // // until the data comes back as response
    // // If your Module(SRAM/Peripheral) can done masking inside of it then it will be good. Otherwise PRESERVE IT!
    
    // val maskedData = responseData.asTypeOf(Vec(4, UInt(8.W)))                       // breaking into Vecs to apply masking
    // data := io.request.bits.activeByteLane.asBools zip maskedData map {                  // applying maskiing a/c to mask bits (activeByteLane)
    //     case (b:Bool, i:UInt) => Mux(b, i, 0.U)
    // }

    // io.response.bits.dataResponse := data.asUInt                            // sending data as response
    // io.response.bits.error := usart.io.intr_tx                            //Mux()   // implement a logic for error here, if the response has error
    // io.response.valid := Mux(usart.io.we , 1.B , 0.B)               // implement a logic for indicating that the requested READ/WRITE operation is done and the response signal coming is valud
    //                                                                // valid pin shall be high for one cycle ONLY
}