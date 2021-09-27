package jigsaw.peripherals.spiflash

case class Config(
    CLK_FREQUENCE : Int = 50000000,
    SPI_FREQUENCE : Int = 50000000,
    CPOL          : Int = 0,
    CPHA          : Int = 0,
    DW            : Int = 32
)