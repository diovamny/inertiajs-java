package com.example.pingcrm.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Replaces the League Glide image server used by the original pingcrm:
 * serves stored files and crops/resizes them via {@code ?w=..&h=..&fit=crop}.
 */
@RestController
public class ImagesController {

    @Value("${pingcrm.images.dir:./data/images}")
    private String imagesDir;

    @GetMapping("/img/**")
    public ResponseEntity<byte[]> show(HttpServletRequest request,
            @RequestParam(name = "w", required = false) Integer width,
            @RequestParam(name = "h", required = false) Integer height,
            @RequestParam(name = "fit", required = false) String fit) throws IOException {
        var path = pathOf(request);
        var base = java.nio.file.Path.of(imagesDir).toAbsolutePath().normalize();
        var target = base.resolve(path).normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target)) {
            return ResponseEntity.notFound().build();
        }

        var bytes = Files.readAllBytes(target);
        var isSvg = path.toLowerCase().endsWith(".svg");
        if (width != null && height != null && width > 0 && height > 0 && !isSvg) {
            var resized = resize(bytes, width, height, "crop".equalsIgnoreCase(fit));
            if (resized != null) {
                bytes = resized;
            }
        }

        var mediaType = Files.probeContentType(target);
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mediaType))
            .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(365)).cachePublic().immutable())
            .body(bytes);
    }

    private static String pathOf(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var prefix = "/img/";
        var index = uri.indexOf(prefix);
        return index >= 0 ? uri.substring(index + prefix.length()) : "";
    }

    private static byte[] resize(byte[] source, int width, int height, boolean crop) throws IOException {
        var input = ImageIO.read(new ByteArrayInputStream(source));
        if (input == null) {
            return null;
        }
        var canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = canvas.createGraphics();
        if (crop) {
            var srcWidth = input.getWidth();
            var srcHeight = input.getHeight();
            var targetRatio = (double) width / height;
            int cropW;
            int cropH;
            if ((double) srcWidth / srcHeight > targetRatio) {
                cropW = (int) Math.round(srcHeight * targetRatio);
                cropH = srcHeight;
            } else {
                cropW = srcWidth;
                cropH = (int) Math.round(srcWidth / targetRatio);
            }
            var x = (srcWidth - cropW) / 2;
            var y = (srcHeight - cropH) / 2;
            graphics.drawImage(input.getSubimage(x, y, cropW, cropH), 0, 0, width, height, null);
        } else {
            graphics.drawImage(input, 0, 0, width, height, null);
        }
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        ImageIO.write(canvas, "png", output);
        return output.toByteArray();
    }
}
