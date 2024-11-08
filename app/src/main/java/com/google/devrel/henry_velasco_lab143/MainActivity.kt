package com.google.devrel.henry_velasco_lab143

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura el modelo TensorFlow Lite
        interpreter = Interpreter(loadModelFile("modelo_clasificacion_retina.tflite"))

        // Cargar la imagen
        val img: ImageView = findViewById(R.id.imageToLabel)
        val fileName = "ejemplo 4.jpg"
        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.let {
            img.setImageBitmap(it)
        }

        // Referencias de UI
        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)

        btn.setOnClickListener {
            bitmap?.let { bmp ->
                val result = classifyImage(bmp)
                txtOutput.text = result
            } ?: run {
                txtOutput.text = "Error: Imagen no encontrada"
            }
        }
    }

    // Función para cargar el modelo .tflite desde assets
    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Función para preprocesar la imagen y hacer la predicción
    private fun classifyImage(bitmap: Bitmap): String {
        // Redimensionar la imagen y normalizar los valores de píxeles
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][y][x][0] = (pixel shr 16 and 0xFF) / 255.0f
                input[0][y][x][1] = (pixel shr 8 and 0xFF) / 255.0f
                input[0][y][x][2] = (pixel and 0xFF) / 255.0f
            }
        }

        // Salida del modelo
        val output = Array(1) { FloatArray(4) }  // Suponiendo 4 clases
        interpreter.run(input, output)

        // Encontrar la clase con la mayor probabilidad
        val labels = arrayOf("Macular_Diseases", "Vascular_Occlusions", "Vitreoretinal_Interface", "No_Pathology")
        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = output[0][maxIdx]

        return "Clase predicha: ${labels[maxIdx]} con probabilidad de ${"%.4f".format(confidence)}"
    }

    // Función para obtener Bitmap desde assets
    private fun assetsToBitmap(fileName: String): Bitmap? {
        return try {
            assets.open(fileName).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
