package com.colcastar.web.Printer

import android.graphics.*

import com.mazenrashed.printooth.data.PrintingImagesHelper
import java.io.ByteArrayOutputStream


class MyPrintingImagesHelper : PrintingImagesHelper {
    override fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray {
        return convertBitmapToByteArray(bitmap)
    }


    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        val factor = 1.1

        val array2 = Utils.decodeBitmap(bitmap)

        val grayBitmap = Other.toGrayscale(bitmap)
        val dithered = Other.thresholdToBWPic(grayBitmap)

        val data = Other.eachLinePixToCmd(dithered, 417, 0)

//        return array2
//        return data
        val array = byteArrayOf(
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1)
        return array2//.sliceArray(0..(53*1267)+8)//.plus(byteArrayOf(0x0D))
    }


}