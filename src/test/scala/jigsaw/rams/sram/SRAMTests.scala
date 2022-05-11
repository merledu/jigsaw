package jigsaw.rams.sram

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.experimental._
import chiseltest._
import chisel3.experimental.BundleLiterals._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation
// import org.scalatest.flatspec.AnyFlatSpec
import jigsaw.SramHarness
import caravan.bus.wishbone.WishboneConfig
import caravan.bus.tilelink.TilelinkConfig

class SRAMTests extends FreeSpec with ChiselScalatestTester {

  // def getFile: Option[String] = {
  //   if (scalaTestContext.value.get.configMap.contains("memFile")) {
  //     Some(scalaTestContext.value.get.configMap("memFile").toString)
  //   } else {
  //     None
  //   }
  //   }

  implicit val config = WishboneConfig(32,32)
  "SRAM" in {
    val programFile = Some("//home//talha//abc.txt")
    test(new SramHarness(programFile))/*.withAnnotations(Seq(VerilatorBackendAnnotation))*/ { c =>
      c.io.req.valid.poke(1.B)
    
      // c.io.csb0.poke(0.B)
      // c.io.web0.poke(0.B)
      // c.io.addr0.poke(4.U)
      // c.io.din0.poke(8.U)
      // c.io.wmask0.poke(15.U)
      // // c.clock.step(1)
      // // c.io.csb0.poke(1.B)
      // c.clock.step(1)
      // c.io.csb0.poke(0.B)
      // c.io.web0.poke(1.B)
      // c.io.addr0.poke(4.U)
      // c.clock.step(1)
      // c.io.csb0.poke(1.B)
      // c.io.web0.poke(0.B)

      var count = 0
      while(count != 15){
          c.io.req.bits.addrRequest.poke(count.U)
          c.io.req.bits.dataRequest.poke(count.U)
          c.io.req.bits.isWrite.poke(1.B)
          c.clock.step(1)
          count += 1
      }
    

    //   c.io.addr0.poke(3.U)
    //   c.clock.step(1)
    //   c.io.addr0.poke(1.U)

      c.clock.step(100)
    }
  }
}
