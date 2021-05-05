package jigsaw.rams.fpga
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
import caravan.bus.wishbone.{WBRequest, WBResponse, WishboneConfig}
import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import chisel3.util.Decoupled

object BlockRam {

  def createNonMaskableRAM[T <: BusConfig]
                          (programFile: Option[String],
                           bus: T,
                           rows: Int) = {
    bus match {
      case bus: WishboneConfig => {
        implicit val config = bus.asInstanceOf[WishboneConfig]
        new BlockRamWithoutMasking(new WBRequest(), new WBResponse(), programFile, rows)
      }
    }
  }

  def createMaskableRAM[T <: BusConfig]
                       (programFile: Option[String],
                        bus: T,
                        rows: Int) = {
    bus match {
      case bus: WishboneConfig => {
        implicit val config = bus.asInstanceOf[WishboneConfig]
        new BlockRamWithMasking(new WBRequest(), new WBResponse(), programFile, rows)
      }
    }
  }
    //implicit val config = bus.asInstanceOf[WishboneConfig]
    //new BlockRamWithoutMasking(new WBRequest(), new WBResponse(), None, rows)

}

class BlockRamWithoutMasking[A <: AbstrRequest, B <: AbstrResponse]
                            (gen: A, gen1: B, programFile: Option[String], rows: Int) extends Module {

  val io = IO(new Bundle {
    val req = Flipped(Decoupled(gen))
    val rsp = Decoupled(gen1)
  })

  // the register that sends valid along with the data read from memory
  // a register is used so that it synchronizes along with the data that comes after one cycle
  val validReg = RegInit(false.B)
  io.rsp.valid := validReg
  io.rsp.bits.error := false.B   // assuming memory controller would never return an error
  io.req.ready := true.B // assuming we are always ready to accept requests from device

  val mem = SyncReadMem(rows, UInt(32.W))

  if(programFile.isDefined) {
    loadMemoryFromFile(mem, programFile.get)
  }

  when(io.req.fire() && !io.req.bits.isWrite) {
    // READ
    io.rsp.bits.dataResponse := mem.read(io.req.bits.addrRequest/4.U)
    validReg := true.B
  } .elsewhen(io.req.fire() && io.req.bits.isWrite) {
    // WRITE
    mem.write(io.req.bits.addrRequest/4.U, io.req.bits.dataRequest)
    validReg := true.B
    io.rsp.bits.dataResponse := DontCare
  } .otherwise {
    validReg := false.B
    io.rsp.bits.dataResponse := DontCare
  }
}

/** TODO: This is left to be done right now */
class BlockRamWithMasking[A <: AbstrRequest, B <: AbstrResponse]
                         (gen: A, gen1: B, programFile: Option[String], rows: Int) extends Module {


  val io = IO(new Bundle {
    val req = Flipped(Decoupled(gen))
    val rsp = Decoupled(gen1)
  })

  val mem = SyncReadMem(rows, Vec(4, UInt((32/4).W)))

  if(programFile.isDefined) {
    loadMemoryFromFile(mem, programFile.get)
  }

  io.rsp.valid := true.B
  io.rsp.bits.dataResponse := 2.U
  io.rsp.bits.error := false.B

}