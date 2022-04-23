package jigsaw.peripherals.i2c

import chisel3._
import chisel3 . util._
import org.scalatest._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

class i2c_Top_Test extends FreeSpec with ChiselScalatestTester {

  "i2c Top TEST" in {
    test(new I2C_Top()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>    


        val addr_bit1 = true.B
        val addr_bit2 = false.B 
        val addr_bit3 = false.B 
        val addr_bit4 = true.B 
        val addr_bit5 = true.B 
        val addr_bit6 = true.B 
        val addr_bit7 = false.B
        
        c.io.we.poke(1.B)  //write enable
        c.clock.step(1)


        //send address
        c.io.addr.poke(4.U)   
        c.clock.step(1) 
        c.io.wdata.poke(78.U)   //1001110
        c.clock.step(1)


        //0 for write data to slave (read/write bit)
        c.io.addr.poke(8.U)
        c.clock.step(1)
        c.io.wdata.poke(0.U)
        c.clock.step(1)

        //start/enable transmission
        c.io.addr.poke(0.U)
        c.clock.step(1)
        c.io.wdata.poke(1.U)
        c.clock.step(1)


       


        //////////////// Slave Addr ACK bit //////////////////////
        c.clock.step(2)
        println(c.io.sda.peek())
        val addr_bit1_peek = c.io.sda.peek()        //peek address bits from sda output
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit2_peek = c.io.sda.peek()
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit3_peek = c.io.sda.peek()
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit4_peek = c.io.sda.peek()
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit5_peek = c.io.sda.peek()
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit6_peek = c.io.sda.peek()
        c.clock.step(1)
        println(c.io.sda.peek())
        val addr_bit7_peek = c.io.sda.peek()

        //compare the send address and slave receive address if matches then send ACK bit 0
        if(addr_bit1_peek.litValue == addr_bit1.litValue & addr_bit2_peek.litValue == addr_bit2.litValue
        & addr_bit3_peek.litValue == addr_bit3.litValue & addr_bit4_peek.litValue == addr_bit4.litValue
        & addr_bit5_peek.litValue == addr_bit5.litValue & addr_bit6_peek.litValue == addr_bit6.litValue
        & addr_bit7_peek.litValue == addr_bit7.litValue){
  
          // if address matches then send 0 ack otherwise 1
          c.io.addr.poke(12.U)
          c.clock.step(1)
          c.io.wdata.poke(0.U)
          c.clock.step(1)


          //after address matches send data to slave for write in slave
          c.io.addr.poke(16.U)
          c.clock.step(1)
          c.io.wdata.poke(109.U) //01101101
          c.clock.step(1)

          c.clock.step(8)                        //wait for data sending (8 bit -> 8 cycles)

          //send ACK bit when data received from master
          c.io.addr.poke(12.U)
          c.clock.step(1)
          c.io.wdata.poke(0.U)
          c.clock.step(1)
          println("Address matches")

        } else{
          //if address not matches with the slave address, slave send NACK 1 
          c.io.addr.poke(12.U)
          c.clock.step(1)
          c.io.wdata.poke(1.U)
          c.clock.step(1)
          println("Address not matches")
        }

        c.io.we.poke(0.B)  //write disable

        


        c.clock.step(50)















        // c.io.start.poke(0.B)
        // c.clock.step(100)
        // val slave_addr = "b1111000".U
        // val slave_data = "b10101011".U

        // c.io.start.poke(1.B)
        // c.io.addr.poke(slave_addr)
        // c.io.data.poke(slave_data)
        // c.io.read_write.poke(0.B)
        

        // ///////////salve//////////
        // // if(slave_addr == "b1010101".U){
        //     c.io.ack.poke(0.B)
        // // } else{
        // //     c.io.ack.poke(1.B)
        // // }


        // // if(slave_data == "b10101111".U){
        //     c.io.data_ack.poke(0.B)
        // // } else{
        // //     c.io.data_ack.poke(1.B)
        // // }


        // //c.io.start.poke(0.B)
        // c.clock.step(50)
        // c.io.start.poke(0.B)
        // c.clock.step(50)

        // c.io.addr.poke(0.U)
        // c.clock.step(1)
        // c.io.wdata.poke(1.U)
        // c.clock.step(1)

        // c.io.addr.poke(4.U)   
        // c.clock.step(1) 
        // c.io.wdata.poke(99.U)   //1100011
        // c.clock.step(1)

        

        // c.io.addr.poke(8.U)
        // c.clock.step(1)
        // c.io.wdata.poke(0.U)
        // c.clock.step(1)

        // c.io.addr.poke(12.U)
        // c.clock.step(1)
        // c.io.wdata.poke(0.U)
        // c.clock.step(1)

        // c.io.addr.poke(16.U)
        // c.clock.step(1)
        // c.io.wdata.poke(135.U) //10000111
        // c.clock.step(1)

        



        // c.io.addr.poke(20.U)
        // c.clock.step(1)
        // c.io.wdata.poke(0.U)
        // c.clock.step(1)


        //c.clock.step(50)





    }
  }
}