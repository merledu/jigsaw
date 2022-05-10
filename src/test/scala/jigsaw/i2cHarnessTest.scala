package jigsaw

import chisel3._
import chisel3 . util._
import org.scalatest._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation
import caravan.bus.wishbone.WishboneConfig
import caravan.bus.tilelink.TilelinkConfig
import caravan.bus.tilelink._

class i2cHarnessTest extends FreeSpec with ChiselScalatestTester {

  "I2C HARNESS TEST" in {
    implicit val config = WishboneConfig(32,32)
    test(new i2cHarness()) { c =>

        val addr_bit1 = false.B
        val addr_bit2 = false.B 
        val addr_bit3 = false.B 
        val addr_bit4 = true.B 
        val addr_bit5 = true.B 
        val addr_bit6 = true.B 
        val addr_bit7 = false.B    

        c.io.request.bits.isWrite.poke(1.B)                   //write enable
        c.clock.step(1)

        //send address
        c.io.request.bits.addrRequest.poke(4.U)
        c.clock.step(1)
        c.io.request.bits.dataRequest.poke(78.U)              //1001110
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(1.B)
        c.io.request.valid.poke(1.B)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(0.B)
        c.io.request.valid.poke(0.B)
        c.clock.step(1)

        //0 for write data to slave (read/write bit)
        c.io.request.bits.addrRequest.poke(8.U)
        c.clock.step(1)
        c.io.request.bits.dataRequest.poke(0.U)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(1.B)
        c.io.request.valid.poke(1.B)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(0.B)
        c.io.request.valid.poke(0.B)
        c.clock.step(1)

        //send data to slave for write in slave
        c.io.request.bits.addrRequest.poke(16.U)
        c.clock.step(1)
        c.io.request.bits.dataRequest.poke(109.U) //01101101
        c.clock.step(1)
        c.io.request.valid.poke(1.B)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(0.B)
        c.io.request.valid.poke(0.B)
        c.clock.step(1)


        //start/enable transmission
        c.io.request.bits.addrRequest.poke(0.U)
        c.clock.step(1)
        c.io.request.bits.dataRequest.poke(1.U)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(1.B)
        c.io.request.valid.poke(1.B)
        c.clock.step(1)
        // c.io.request.bits.isWrite.poke(0.B)
        c.io.request.valid.poke(0.B)
        c.clock.step(1)


        //////////////// Slave Addr ACK bit //////////////////////
        c.clock.step(2)
        println(c.io.i2c_sda.peek())
        val addr_bit1_peek = c.io.i2c_sda.peek()        //peek address bits from i2c_sda output
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
  
          // if address matches then send 0 ack otherwise 1
          c.io.request.bits.addrRequest.poke(12.U)
          c.clock.step(1)
          c.io.request.bits.dataRequest.poke(0.U)
          c.clock.step(1)
          c.io.request.valid.poke(1.B)
          c.clock.step(1)
          // c.io.request.bits.isWrite.poke(0.B)
          c.io.request.valid.poke(0.B)
          c.clock.step(1)


         

          //c.clock.step(8)                        //wait for data sending (8 bit -> 8 cycles)

          //send ACK bit when data received from master
          c.io.request.bits.addrRequest.poke(12.U)
          c.clock.step(1)
          c.io.request.bits.dataRequest.poke(0.U)
          c.clock.step(1)
          c.io.request.valid.poke(1.B)
          c.clock.step(1)
          // c.io.request.bits.isWrite.poke(0.B)
          c.io.request.valid.poke(0.B)
          c.clock.step(1)
          println("Address matches")

        } else{
          //if address not matches with the slave address, slave send NACK 1 
          c.io.request.bits.addrRequest.poke(12.U)
          c.clock.step(1)
          c.io.request.bits.dataRequest.poke(1.U)
          c.clock.step(1)
          c.io.request.valid.poke(1.B)
          c.clock.step(1)
          // c.io.request.bits.isWrite.poke(0.B)
          c.io.request.valid.poke(0.B)
          c.clock.step(1)
          println("Address not matches")
        }

        c.io.request.bits.isWrite.poke(0.B)  //write disable

       
        c.clock.step(100)


    }
  }
}