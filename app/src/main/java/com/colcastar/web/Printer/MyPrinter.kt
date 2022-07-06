package com.colcastar.web.Printer

import com.mazenrashed.printooth.data.PrintingImagesHelper
import com.mazenrashed.printooth.data.converter.Converter
import com.mazenrashed.printooth.data.converter.DefaultConverter
import com.mazenrashed.printooth.data.printer.Printer

open class MyPrinter : Printer() {

    override fun initLineSpacingCommand(): ByteArray = byteArrayOf(0x1B, 0x33)

    override fun initInitPrinterCommand(): ByteArray = byteArrayOf(0x1B, 0x40)

    override fun initJustificationCommand(): ByteArray = byteArrayOf(27, 97)

    override fun initFontSizeCommand(): ByteArray = byteArrayOf(29, 33)

    override fun initEmphasizedModeCommand(): ByteArray = byteArrayOf(27, 69)

    override fun initUnderlineModeCommand(): ByteArray = byteArrayOf(27, 45)

    override fun useConverter(): Converter  = DefaultConverter()

    override fun initCharacterCodeCommand(): ByteArray = byteArrayOf(27, 116)

    override fun initFeedLineCommand(): ByteArray = byteArrayOf(27, 100)

    override fun initPrintingImagesHelper(): PrintingImagesHelper = MyPrintingImagesHelper()
}