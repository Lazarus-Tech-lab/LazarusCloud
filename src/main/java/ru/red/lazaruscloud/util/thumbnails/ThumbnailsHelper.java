package ru.red.lazaruscloud.util.thumbnails;

import net.coobird.thumbnailator.Thumbnails;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ThumbnailsHelper {

    public static String getThumbnailFromFile(Path filePath, String userRootFolder, String serverFileName) {

        final String serverUserPathStorage = Paths.get("uploads", userRootFolder)+"/thumb_"+serverFileName+".jpeg";
        try{
            System.out.println(MimeType.getMime(Files.probeContentType(filePath)));

            switch (MimeType.getMime(Files.probeContentType(filePath))) {
                case IMAGE -> {
                    Thumbnails.of(filePath.toString())
                            .size(600, 600)
                            .toFile(serverUserPathStorage);

                    return serverUserPathStorage;
                }

                case AUDIO -> {
                    File mp3File = new File(String.valueOf(filePath));
                    AudioFile audioFile = null;
                    try {
                        audioFile = AudioFileIO.read(mp3File);
                    } catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                        throw new RuntimeException(e);
                    }
                    Tag tag = audioFile.getTag();
                    Artwork artwork = tag.getFirstArtwork();

                    if (artwork != null) {
                        byte[] imageData = artwork.getBinaryData();

                        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageData));


                        BufferedImage resizedImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g = resizedImage.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g.drawImage(originalImage, 0, 0, 600, 600, null);
                        g.dispose();

                        // Сохраняем обложку в файл (JPEG)
                        File outputFile = new File(serverUserPathStorage);
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            ImageIO.write(resizedImage, "jpeg", fos);
                        }

                        return serverUserPathStorage;
                    } else {
                        return "uploads/pictures/music.png";
                    }
                }
                case MD -> {
                    return "uploads/pictures/md.png";
                }
                default -> {
                    return "uploads/pictures/file.png";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "uploads/pictures/file.png";
    }
}
