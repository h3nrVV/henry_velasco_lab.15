package com.google.devrel.henry_velasco_lab143

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración edge-to-edge; usa WindowCompat como alternativa estándar.
        // WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        // Configuración del modelo ML Kit
        val localModel = LocalModel.Builder()
            .setAssetFilePath("model.tflite")
            .build()

        // Obtiene referencia al ImageView y carga la imagen desde assets
        val img: ImageView = findViewById(R.id.imageToLabel)
        val fileName = "ejemplo1.jpeg"
        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }

        // Referencias a los elementos de UI
        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)

        // Configura el botón para procesar la imagen
        btn.setOnClickListener {
            val options = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.3f)
                .setMaxResultCount(4)
                .build()
            val labeler = ImageLabeling.getClient(options)

            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                var outputText = ""
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // Procesa los resultados exitosos
                        for (label in labels) {
                            val text = label.text
                            val confidence = label.confidence
                            outputText += "$text : $confidence\n"
                        }
                        txtOutput.text = outputText
                    }
                    .addOnFailureListener { e ->
                        // Manejo básico de errores
                        e.printStackTrace()
                        txtOutput.text = "Error: ${e.message}"
                    }
            } else {
                txtOutput.text = "Error: Imagen no encontrada"
            }
        }

        // Manejo de padding para el modo "edge-to-edge"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función para obtener Bitmap desde assets
    private fun Context.assetsToBitmap(fileName: String): Bitmap? {
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
