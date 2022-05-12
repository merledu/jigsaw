package jigsaw.peripherals.i2c

import chisel3._
import chisel3 . util._
import org.scalatest._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

class i2cTest extends FreeSpec with ChiselScalatestTester {

  "i2c TEST" in {
    test(new i2c_master()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>    

        
        val addr_bit1 = true.B
        val addr_bit2 = false.B 
        val addr_bit3 = false.B 
        val addr_bit4 = true.B 
        val addr_bit5 = true.B 
        val addr_bit6 = true.B 
        val addr_bit7 = false.B


        c.io.addr.poke("b1001110".U)   //poke address
        c.io.read_write.poke(0.B)      // 0 for write data to slave (read/write bit)
        c.io.start.poke(1.B)          // enable
        



        //////////////// Slave Addr ACK bit //////////////////////
        c.clock.step(2)
        println(c.io.i2c_sda.peek())
        val addr_bit1_peek = c.io.i2c_sda.peek()        //peek address bits from sda output
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit2_peek = c.io.i2c_sda.peek()
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit3_peek = c.io.i2c_sda.peek()
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit4_peek = c.io.i2c_sda.peek()
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit5_peek = c.io.i2c_sda.peek()
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit6_peek = c.io.i2c_sda.peek()
        c.clock.step(1)
        println(c.io.i2c_sda.peek())
        val addr_bit7_peek = c.io.i2c_sda.peek()
        
        //compare the send address and slave receive address if matches then send ACK bit 0
        if(addr_bit1_peek.litValue == addr_bit1.litValue & addr_bit2_peek.litValue == addr_bit2.litValue
        & addr_bit3_peek.litValue == addr_bit3.litValue & addr_bit4_peek.litValue == addr_bit4.litValue
        & addr_bit5_peek.litValue == addr_bit5.litValue & addr_bit6_peek.litValue == addr_bit6.litValue
        & addr_bit7_peek.litValue == addr_bit7.litValue){
  
          c.io.i2c_sda_in.poke(0.B)           // if address matches then send 0 ack otherwise 1
          c.io.data.poke("b1101101".U)        //after address matches send data to slave for write in slave
          c.clock.step(8)
          c.io.i2c_sda_in.poke(0.B)           //send ACK bit when data received from master
          println("Address matches")

        } else{
          // c.clock.step(1)
          c.io.i2c_sda_in.poke(1.B)         //if address not matches with the slave address, slave send NACK 1 
          println("Address not matches")
        }

        //////////////// Slave Addr ACK bit //////////////////////

        c.clock.step(50)
        c.io.start.poke(0.B)
        c.clock.step(50)

    }
  }
}