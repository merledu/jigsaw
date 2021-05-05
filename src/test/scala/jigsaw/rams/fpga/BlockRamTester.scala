package jigsaw.rams.fpga
import caravan.bus.wishbone.WishboneConfig
import org.scalatest._
import chiseltest._
import chisel3._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

trait MemoryDumpFileHelper { self: FreeSpec with ChiselScalatestTester =>
  def getFile: Option[String] = {
    if (scalaTestContext.value.get.configMap.contains("memFile")) {
      Some(scalaTestContext.value.get.configMap("memFile").toString)
    } else {
      None
    }
  }
}

class BlockRamTester extends FreeSpec with ChiselScalatestTester with MemoryDumpFileHelper {

  "write and read to single location" in {
    val programFile = getFile
    test(BlockRam.createNonMaskableRAM(programFile, WishboneConfig(32,32), 1024)).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      c.clock.step(5)
      sendRequest(0.U, 10.U, "b1111".U, true.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now sending a read request")

      sendRequest(0.U, 10.U, "b1111".U, false.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now reading expected data")
      c.io.rsp.bits.dataResponse.expect(10.U)
      println("EXPECTED DATA IS: 10 GOT " + c.io.rsp.bits.dataResponse.peek().litValue().toInt.toString)
      c.clock.step(4)

      def sendRequest(addr: UInt, data: UInt, byteLane: UInt, isWrite: Bool): Unit = {
        c.clock.step(1)
        c.io.req.valid.poke(true.B)
        c.io.req.bits.addrRequest.poke(addr)
        c.io.req.bits.dataRequest.poke(data)
        c.io.req.bits.activeByteLane.poke(byteLane)
        c.io.req.bits.isWrite.poke(isWrite)
        c.clock.step(1)
        c.io.req.valid.poke(false.B)
      }
    }
  }

  "initialize mem with programFile and read" in {
    val programFile = getFile
    test(BlockRam.createNonMaskableRAM(programFile, WishboneConfig(32,32), 1024)).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      c.clock.step(5)
      sendRequest(0.U, 0.U, "b1111".U, false.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now reading expected data")
      c.io.rsp.bits.dataResponse.expect("h00100113".U)
      println("EXPECTED DATA IS: " + "h00100113".U.litValue().toInt + " GOT " + c.io.rsp.bits.dataResponse.peek().litValue().toInt.toString)

      c.clock.step(1)

      sendRequest(4.U, 0.U, "b1111".U, false.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now reading expected data")
      c.io.rsp.bits.dataResponse.expect("h00200193".U)
      println("EXPECTED DATA IS: " + "h00200193".U.litValue().toInt + " GOT " + c.io.rsp.bits.dataResponse.peek().litValue().toInt.toString)

      sendRequest(8.U, 0.U, "b1111".U, false.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now reading expected data")
      c.io.rsp.bits.dataResponse.expect("h00C000EF".U)
      println("EXPECTED DATA IS: " + "h00C000EF".U.litValue().toInt + " GOT " + c.io.rsp.bits.dataResponse.peek().litValue().toInt.toString)

      c.clock.step(1)

      sendRequest(12.U, 0.U, "b1111".U, false.B)
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      while(c.io.rsp.valid.peek().litToBoolean != true) {
        println("wait")
        c.clock.step(1)
      }
      println("VALID RESPONSE = " + c.io.rsp.valid.peek().litToBoolean.toString)
      println("Got the response now reading expected data")
      c.io.rsp.bits.dataResponse.expect("h00100793".U)
      println("EXPECTED DATA IS: " + "h00100793".U.litValue().toInt + " GOT " + c.io.rsp.bits.dataResponse.peek().litValue().toInt.toString)

      c.clock.step(3)

      def sendRequest(addr: UInt, data: UInt, byteLane: UInt, isWrite: Bool): Unit = {
        c.clock.step(1)
        c.io.req.valid.poke(true.B)
        c.io.req.bits.addrRequest.poke(addr)
        c.io.req.bits.dataRequest.poke(data)
        c.io.req.bits.activeByteLane.poke(byteLane)
        c.io.req.bits.isWrite.poke(isWrite)
        c.clock.step(1)
        c.io.req.valid.poke(false.B)
      }
    }
  }

//  "just work for maskable bram" in {
//    test(new BlockRamWithMasking(10, 32, None)).withAnnotations(Seq(VerilatorBackendAnnotation)) {c =>
//      c.io.addr.poke(0.U)
//      c.io.write.poke(true.B)
//      c.io.enable.poke(false.B)
//      c.io.mask.map(b => b.poke(true.B))
//      c.io.wrData.map(d => d.poke("hab".U))
//      c.clock.step(2)
//      c.io.enable.poke(true.B)
//      c.io.rdData.foreach(d => d.expect("hab".U))
//    }
//  }

}
