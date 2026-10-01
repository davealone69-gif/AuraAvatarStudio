package com.aura.avatarstudio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@UnstableApi
object VideoTransformer {

    data class Options(
        val trimStartMs: Long = 0,
        val trimEndMs: Long? = null,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val rotationDegrees: Float = 0f,
        // H.264 is widely supported for hardware encode; H.265 often fails on mid-range devices
        val videoMime: String = MimeTypes.VIDEO_H264,
        val audioMime: String = MimeTypes.AUDIO_AAC,
    )

    suspend fun export(
        context: Context,
        inputUri: Uri,
        options: Options = Options(),
    ): File = suspendCancellableCoroutine { cont ->
        val dir = File(context.filesDir, "exports").also { if (!it.exists()) it.mkdirs() }
        val outFile = File(dir, "export_${System.currentTimeMillis()}.mp4")

        val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)
        if (options.trimStartMs > 0 || options.trimEndMs != null) {
            val clipping = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(options.trimStartMs.coerceAtLeast(0L))
                .apply {
                    options.trimEndMs?.let { end ->
                        if (end > options.trimStartMs) setEndPositionMs(end)
                    }
                }
                .build()
            mediaItemBuilder.setClippingConfiguration(clipping)
        }
        val mediaItem = mediaItemBuilder.build()

        val videoEffects = mutableListOf<androidx.media3.common.Effect>()
        if (options.scaleX != 1f || options.scaleY != 1f || options.rotationDegrees != 0f) {
            videoEffects += ScaleAndRotateTransformation.Builder()
                .setScale(options.scaleX, options.scaleY)
                .setRotationDegrees(options.rotationDegrees)
                .build()
        }

        val edited = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(emptyList(), videoEffects))
            .build()

        val transformer = Transformer.Builder(context.applicationContext)
            .setVideoMimeType(options.videoMime)
            .setAudioMimeType(options.audioMime)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (cont.isActive) {
                        if (outFile.exists() && outFile.length() > 0L) {
                            cont.resume(outFile)
                        } else {
                            cont.resumeWithException(
                                IllegalStateException("Export finished but output file is missing or empty")
                            )
                        }
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException,
                ) {
                    if (cont.isActive) cont.resumeWithException(exportException)
                }
            })
            .build()

        cont.invokeOnCancellation {
            try {
                transformer.cancel()
            } catch (_: Exception) {
            }
        }

        try {
            transformer.start(edited, outFile.absolutePath)
        } catch (e: Exception) {
            if (cont.isActive) cont.resumeWithException(e)
        }
    }
}
