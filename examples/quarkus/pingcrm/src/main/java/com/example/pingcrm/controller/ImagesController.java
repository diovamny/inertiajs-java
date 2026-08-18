package com.example.pingcrm.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.Blocking;

/**
 * Replaces the League Glide image server used by the original pingcrm:
 * serves stored files and crops/resizes them via {@code ?w=..&h=..&fit=crop}.
 */
@Path("/img")
public class ImagesController {

    @ConfigProperty(name = "pingcrm.images.dir", defaultValue = "./data/images")
    String imagesDir;

    @GET
    @Path("{path:.*}")
    @Blocking
    public Response show(@PathParam("path") String path,
            @QueryParam("w") Integer width,
            @QueryParam("h") Integer height,
            @QueryParam("fit") String fit) throws IOException {
        var base = java.nio.file.Path.of(imagesDir).toAbsolutePath().normalize();
        var target = base.resolve(path).normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target)) {
            return Response.status(Response.Status.NOT_FOUND).build();
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
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return Response.ok(bytes, mediaType)
            .header("Cache-Control", "public, max-age=31536000, immutable")
            .build();
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
