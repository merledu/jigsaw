package jigsaw.peripherals.timer

import chisel3._
import chisel3.util._
import caravan.bus.common.{AbstrRequest, AbstrResponse}

class Timer_IO[A <: AbstrRequest, B <: AbstrResponse] (gen: A, gen1: B) extends Bundle{

    // bus interconnect interfaces
    val req = Flipped(Decoupled(gen))
    val rsp = Decoupled(gen1)

    val cio_timer_intr_cmp = Output(Bool())
    val cio_timer_intr_ovf = Output(Bool())
}


// timer in chisel
class Timer[A <: AbstrRequest, B <: AbstrResponse] (gen: A, gen1: B) extends Module{
    val io = IO(new Timer_IO(gen,gen1))

    // registers
    val TimerReg   = RegInit(0.U(32.W)) // RO 0x0
    val ControlReg = RegInit(0.U(32.W)) // RW 0x4
    val CompareReg = RegInit(0.U(32.W)) // RW 0x8
    val PreCountReg= RegInit(0.U(32.W)) // RO 0xC

    val maskedData = Wire(Vec(4, UInt(8.W)))
    maskedData := io.req.bits.activeByteLane.asTypeOf(Vec(4, Bool())) map (Fill(8,_))

    io.req.ready := 1.B
    io.rsp.valid := 0.B

    // io.cio_timer_intr_cmp := DontCare
    
    when (io.req.bits.addrRequest(3,0) === 0.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, TimerReg, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .elsewhen (io.req.bits.addrRequest(3,0) === 4.U && io.req.bits.isWrite === 1.B){
        ControlReg := Mux(io.req.valid, io.req.bits.dataRequest & maskedData.asUInt, ControlReg)

        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, io.req.bits.dataRequest, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .elsewhen (io.req.bits.addrRequest(3,0) === 4.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, ControlReg, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .elsewhen (io.req.bits.addrRequest(3,0) === 8.U && io.req.bits.isWrite === 1.B){
        CompareReg := Mux(io.req.valid, io.req.bits.dataRequest & maskedData.asUInt, CompareReg)

        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, io.req.bits.dataRequest, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .elsewhen (io.req.bits.addrRequest(3,0) === 8.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, CompareReg, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .elsewhen (io.req.bits.addrRequest(3,0) === 8.U && io.req.bits.isWrite === 0.B){
        io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready, PreCountReg, 0.U))
        io.rsp.valid := RegNext(io.req.valid)
    }
    .otherwise{
        List(io.cio_timer_intr_cmp, io.rsp.valid) map (_ := DontCare)
        io.rsp.bits.dataResponse := RegNext(io.req.bits.addrRequest)
    }

    val enable = WireInit(ControlReg(0))
    val prescalar = WireInit(ControlReg(31,1))

    // Prescalar+Timer Logic
    when(PreCountReg === 0.U && enable){
        PreCountReg := PreCountReg + 1.U
    }
    .elsewhen(TimerReg === CompareReg || TimerReg === "hffffffff".U){
        TimerReg := 0.U
    }
    .elsewhen(PreCountReg === prescalar && enable){
        TimerReg := TimerReg + 1.U
        PreCountReg := 0.U
    }.elsewhen(PreCountReg < prescalar){
        PreCountReg := PreCountReg + 1.U
    }

    // Interupts
    io.cio_timer_intr_cmp := Mux(enable, TimerReg === CompareReg, 0.B)
    io.cio_timer_intr_ovf := Mux(enable, TimerReg === "hffffffff".U, 0.B)



    // Error Logic
    val addr_hit = Wire(Vec(4, Bool()))
    val timerRegMap = Seq(0,4,8,12)
    val wireAddr = WireInit(io.req.bits.addrRequest(3,0))
    val addr_miss = Wire(Bool())
    
    def go(min:Int, max:Int):Unit = {
        if (min == max){
            return
        }else{
            addr_hit(min) := wireAddr === timerRegMap(min).asUInt()
            go(min+1, max)
        }
    }

    go(0,4)


    addr_miss := ~addr_hit.reduce(_ | _)//~addr_hit.contains(true.B)
    when(wireAddr === 0.U & io.req.bits.isWrite){io.rsp.bits.error := RegNext(io.req.valid)}
    .elsewhen(wireAddr === 12.U & io.req.bits.isWrite){io.rsp.bits.error := RegNext(io.req.valid)}
    .otherwise{io.rsp.bits.error := RegNext(io.req.valid & addr_miss)}
}