package jigsaw.peripherals.spi

case class Config(
    CLK_FREQUENCE : Int = 15000000,
    SPI_FREQUENCE : Int = 5000000,
    CPOL          : Int = 1,
    CPHA          : Int = 1,
    DW            : Int = 32
)