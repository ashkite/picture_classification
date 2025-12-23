package com.ashkite.pictureclassification.data.ml

import android.content.Context
import android.graphics.Bitmap
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter

class TfliteImageClassifier(
    context: Context,
    modelAssetPath: String = DEFAULT_MODEL_PATH,
    labelsAssetPath: String = DEFAULT_LABELS_PATH
) : Closeable {
    private val interpreter: Interpreter
    private val labels: List<String>
    val inputWidth: Int
    val inputHeight: Int
    private val inputType: DataType

    init {
        val model = loadModel(context, modelAssetPath)
        interpreter = Interpreter(model)
        val inputShape = interpreter.getInputTensor(0).shape()
        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        inputType = interpreter.getInputTensor(0).dataType()
        labels = context.assets.open(labelsAssetPath).bufferedReader().useLines { it.toList() }
    }

    fun classify(bitmap: Bitmap, topK: Int = 5): List<LabelScore> {
        val resized = if (bitmap.width == inputWidth && bitmap.height == inputHeight) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        }
        val inputBuffer = when (inputType) {
            DataType.FLOAT32 -> toFloatBuffer(resized)
            DataType.UINT8 -> toUInt8Buffer(resized)
            else -> toFloatBuffer(resized)
        }
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)
        val scores = output[0]
        val results = scores.indices.map { idx ->
            val label = labels.getOrNull(idx) ?: "label_$idx"
            LabelScore(label = label, score = scores[idx])
        }
        return results.sortedByDescending { it.score }.take(topK)
    }

    override fun close() {
        interpreter.close()
    }

    private fun toFloatBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        pixels.forEach { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            buffer.putFloat(r / 255f)
            buffer.putFloat(g / 255f)
            buffer.putFloat(b / 255f)
        }
        buffer.rewind()
        return buffer
    }

    private fun toUInt8Buffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        pixels.forEach { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            buffer.put(r.toByte())
            buffer.put(g.toByte())
            buffer.put(b.toByte())
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModel(context: Context, assetPath: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { input ->
            val channel = input.channel
            return channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    companion object {
        const val DEFAULT_MODEL_PATH = "models/event_model.tflite"
        const val DEFAULT_LABELS_PATH = "models/event_labels.txt"
    }
}
