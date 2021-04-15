package jigsaw.rams.fpga
import org.scalatest._
import chiseltest._
import chisel3._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

class BlockRamTester extends FreeSpec with ChiselScalatestTester {

  "just work for non-maskable bram" in {
    test(new BlockRamWithoutMasking(10, 32, None)).withAnnotations(Seq(VerilatorBackendAnnotation)) {c =>
      c.io.addr.poke(0.U)
      c.io.write.poke(true.B)
      c.io.enable.poke(false.B)
      c.io.wrData.poke(10.U)
      c.clock.step(2)
      c.io.enable.poke(true.B)
      c.io.rdData.expect(10.U)
    }
  }

  "just work for maskable bram" in {
    test(new BlockRamWithMasking(10, 32, None)).withAnnotations(Seq(VerilatorBackendAnnotation)) {c =>
      c.io.addr.poke(0.U)
      c.io.write.poke(true.B)
      c.io.enable.poke(false.B)
      c.io.mask.map(b => b.poke(true.B))
      c.io.wrData.map(d => d.poke("hab".U))
      c.clock.step(2)
      c.io.enable.poke(true.B)
      c.io.rdData.foreach(d => d.expect("hab".U))
    }
  }

}
