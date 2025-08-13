package ru.red.lazaruscloud.util.thumbnails;

public enum MimeType {
    AUDIO,
    VIDEO,
    IMAGE,
    TEXT,
    MD,
    PP,
    DOC;

    public static MimeType getMime(String mimeType) {
        switch (mimeType) {
            case "image/jpeg", "image/png", "image/webp" -> {
                return IMAGE;
            }

            case "audio/mpeg", "audio/flac", "audio/x-wav" -> {
                return AUDIO;
            }
            case "video/mp4", "video/mpeg", "video/x-msvideo", "video/x-matroska" -> {
                return VIDEO;
            }
            case "text/markdown" -> {
                return MD;
            }

            case null, default -> {
                return TEXT;
            }
        }

    }
}
