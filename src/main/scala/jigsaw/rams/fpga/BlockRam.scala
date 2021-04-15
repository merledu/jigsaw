package jigsaw.rams.fpga
import chisel3._
import chisel3.util.experimental.{loadMemoryFromFile, loadMemoryFromFileInline}
import chisel3.experimental.{ChiselAnnotation, annotate}

class BlockRamWithoutMaskingBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val addr = Input(UInt(addrWidth.W))
  val write = Input(Bool())
  val enable = Input(Bool())
  val wrData = Input(UInt(dataWidth.W))
  val rdData = Output(UInt(dataWidth.W))
}

class BlockRamWithMaskingBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val addr = Input(UInt(addrWidth.W))
  val write = Input(Bool())
  val enable = Input(Bool())
  val mask = Input(Vec(4, Bool()))
  val wrData = Input(Vec(4, UInt((dataWidth/4).W)))
  val rdData = Output(Vec(4, UInt((dataWidth/4).W)))
}

class BlockRamWithoutMasking(
                addrWidth: Int,
                dataWidth: Int,
                programFile: Option[String]) extends Module {

  val io = IO(new BlockRamWithoutMaskingBundle(addrWidth, dataWidth))

  val mem = SyncReadMem(Math.pow(2, addrWidth).toInt, UInt(dataWidth.W))

  if(programFile.isDefined) {
    loadMemoryFromFile(mem, programFile.get)
  }

  io.rdData := mem.read(io.addr, io.enable)
  when(io.write) {
      mem.write(io.addr, io.wrData)
  }

}

class BlockRamWithMasking(
                   addrWidth: Int,
                   dataWidth: Int,
                   programFile: Option[String]) extends Module {

  val io = IO(new BlockRamWithMaskingBundle(addrWidth, dataWidth))

  //  val mem = if (maskable) {
  //    SyncReadMem(Math.pow(2, addrWidth).toInt, Vec(4, UInt((dataWidth/8).W)))
  //  } else {
  //    SyncReadMem(Math.pow(2, addrWidth).toInt, UInt(dataWidth.W))
  //  }

  val mem = SyncReadMem(Math.pow(2, addrWidth).toInt, Vec(4, UInt((dataWidth/4).W)))

  if(programFile.isDefined) {
    loadMemoryFromFile(mem, programFile.get)
  }

  io.rdData := mem.read(io.addr, io.enable)
  when(io.write) {
    mem.write(io.addr, io.wrData, io.mask)
  }

}