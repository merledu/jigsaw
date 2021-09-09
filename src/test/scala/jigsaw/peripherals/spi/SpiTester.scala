package jigsaw.peripherals.spi

import chisel3._
import chisel3 . util._
import caravan.bus.wishbone._
import org.scalatest._
import chisel3.experimental._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation
// import org.scalatest.flatspec.AnyFlatSpec



class SpiTester extends FreeSpec with ChiselScalatestTester {

//   "Spi Master Tests" in {
//     test(new SpiMaster()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
//         c.clock.step(20)
//     }
//   }

//   "Spi Slave Tests" in {
//     test(new SpiSlave()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
//         c.clock.step(20)
//     }
//   }

//   "Spi Wrapper Tests" in {
//     test(new SpiWrapper()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
//         c.clock.step(20)
//     }
//   }

  "Spi" in {
    implicit val config = WishboneConfig(10, 32)
    test(new Spi()).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>

      c.clock.step(5)
      c.io.valid.poke(true.B)
      c.io.addrReq.poke(0.U)
      c.io.dataReq.poke("b1010101010101010101010100001101".U)
      c.io.byteLane.poke("b1111111111111111111111111111111".U)
      c.io.isWrite.poke(true.B)
      c.clock.step(1)
      c.io.valid.poke(false.B)
      c.clock.step(5)
      c.io.valid.poke(true.B)
      c.io.addrReq.poke(0.U)
      c.io.dataReq.poke("b1111100111100000000001111111111".U)
      c.io.byteLane.poke("b1111111111111111111111111111111".U)
      c.io.isWrite.poke(true.B)
      c.clock.step(1)
      c.io.valid.poke(false.B)

      c.clock.step(1000)
    }
  }
}