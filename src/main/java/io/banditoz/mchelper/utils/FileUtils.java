package io.banditoz.mchelper.utils;

import java.io.*;
import java.util.UUID;

import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
    private FileUtils() {}

    /**
     * Optionally compresses a PNG.
     *
     * @param image PNG image, as a ByteArrayOutputStream
     * @return A FileUpload holding a randomized file name and the compressed image. If uploading to Discord, the
     * underlying {@link FileInputStream} will be closed.
     */
    public static FileUpload compressPNG(ByteArrayOutputStream image) {
        String imageName = UUID.randomUUID().toString().replace("-", "") + ".png";
        File f = new File(imageName);

        try (OutputStream outputStream = new FileOutputStream(imageName)) {
            image.writeTo(outputStream);

            Process p = new ProcessBuilder("oxipng", imageName).start();
            p.waitFor();

            return FileUpload.fromData(new FileInputStream(f), imageName);
        } catch (IOException | InterruptedException ex) {
            log.warn("Error compressing PNG data, returning image as-is.", ex);
            return FileUpload.fromData(image.toByteArray(), imageName);
        }
    }
}
