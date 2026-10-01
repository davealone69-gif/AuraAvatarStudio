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
        val videoMime: String = MimeTypes.VIDEO_H265,
        val audioMime: String = MimeTypes.AUDIO_AAC,
    )

    suspend fun export(
        context: Context,
        inputUri: Uri,
        options: Options = Options(),
    ): File = suspendCancellableCoroutine { cont ->
        val outFile = File(context.cacheDir, "export_${System.currentTimeMillis()}.mp4")

        val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)
        if (options.trimStartMs > 0 || options.trimEndMs != null) {
            val clipping = MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(options.trimStartMs)
                .apply { options.trimEndMs?.let { setEndPositionMs(it) } }
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
            .setEffects(Effects(/* audioProcessors */ emptyList(), videoEffects))
            .build()

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(options.videoMime)
            .setAudioMimeType(options.audioMime)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (cont.isActive) cont.resume(outFile)
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

        cont.invokeOnCancellation { transformer.cancel() }
        transformer.start(edited, outFile.absolutePath)
    }
}
