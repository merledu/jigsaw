package jigsaw.peripherals.UART

import chisel3._
import chisel3 . util._
import org.scalatest._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation


class uartRX_Test extends FreeSpec with ChiselScalatestTester{
    "uart rx" in {
        test(new uartRX()).withAnnotations(Seq(VerilatorBackendAnnotation)){c =>
            // c.io.in(0).bits.poke(1.U)
            // c.io.in(1).bits.poke(2.U)
            // c.io.in(0).valid.poke(false.B)
            // c.io.in(1).valid.poke(true.B)
            // c.clock.step(200)
            //c.io.out.expect(0.B)

            c.io.CLKS_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)
                                                            //0101001101
            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(1.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(0.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(1.B)
            c.clock.step(87)

            //c.io.CLK_PER_BIT.poke(87.U)
            c.io.i_Rx_Serial.poke(1.B)
            c.clock.step(87)
            

            c.clock.setTimeout(0)
            c.clock.step(8000)
            //c.io.tx_en.poke(0.B)
            //c.clock.step(300)
            
    }
    }
	
}
