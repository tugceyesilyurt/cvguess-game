package cvguess.core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessor {

    // Görüntüyü merkezden kare crop yapıp hedef boyuta ölçekler
    public BufferedImage centerCropAndResize(BufferedImage src, int targetSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        int side = Math.min(w, h);

        int x = (w - side) / 2;
        int y = (h - side) / 2;

        BufferedImage cropped = src.getSubimage(x, y, side, side);

        BufferedImage out = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(cropped, 0, 0, targetSize, targetSize, null);
        g.dispose();
        return out;
    }

    // Basit renk quantization: her kanalı belirli seviyeye yuvarlar (K-means değil ama “kümeleme benzeri”)
    public BufferedImage quantizeColors(BufferedImage src, int levelsPerChannel) {
        if (levelsPerChannel < 2) levelsPerChannel = 2;
        int step = 256 / levelsPerChannel;

        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = (r / step) * step;
                g = (g / step) * step;
                b = (b / step) * step;

                int q = (r << 16) | (g << 8) | b;
                out.setRGB(x, y, q);
            }
        }
        return out;
    }
}
