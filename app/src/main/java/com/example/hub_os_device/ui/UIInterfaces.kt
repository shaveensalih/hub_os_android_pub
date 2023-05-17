package com.example.hub_os_device.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

interface QRCodeInterface {
    fun getQrCodeBitmap(link: String, width: Int, height: Int): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().also {
            it[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            it[EncodeHintType.MARGIN] = 0
        } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(
            link,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints,
        )
        return Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        ).also {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}

interface LooperInterface {
    var mHandler: Handler?
    var mRunnable: Runnable?
    var mTime: Long?

    fun init(handler: Handler, time: Long, callback: () -> Unit) {
        mHandler = handler
        mRunnable = Runnable(callback)
        mTime = time
    }

    fun startHandler() {
        mHandler?.postDelayed(mRunnable!!, mTime!!)
    }

    fun stopHandler() {
        mHandler?.removeCallbacks(mRunnable!!)
    }
}