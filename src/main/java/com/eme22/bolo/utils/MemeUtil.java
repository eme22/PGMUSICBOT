package com.eme22.bolo.utils;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MemeUtil {

    private final Font font;
    private final static int width = 1000, height = 500;
    private final BufferedImage memeBaseImage;
    private final BufferedImage memeImage;
    private final Graphics2D g2d;

    public MemeUtil(BufferedImage image) {

        this.memeBaseImage = image;

        // Crear una nueva imagen de 1500x500
        memeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Obtener el contexto gráfico de la nueva imagen
        g2d = memeImage.createGraphics();

        // Configurar la fuente y la métrica de fuente para el texto
        font = new Font("Impact", Font.BOLD, 50);
    }

    public BufferedImage generateMeme(String textoSuperior, String textoInferior) {


        //int newWidth = new Double(memeBaseImage.getWidth() * width / memeBaseImage.getWidth()).intValue();
        //int newHeight = new Double(memeBaseImage.getHeight() * height / memeBaseImage.getHeight()).intValue();

        g2d.drawImage(Scalr.resize(memeBaseImage, Scalr.Method.AUTOMATIC, 0, height), 0, 0, null);

        // Establecer la fuente y el tamaño del texto superior e inferior
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);

        // Dibujar el texto superior centrado en la parte superior de la imagen
        int anchoTextoSuperior = g2d.getFontMetrics().stringWidth(textoSuperior);
        g2d.drawString(textoSuperior, (width-anchoTextoSuperior)/2, 50);

        // Dibujar el texto inferior centrado en la parte inferior de la imagen
        int anchoTextoInferior = g2d.getFontMetrics().stringWidth(textoInferior);
        g2d.drawString(textoInferior, (width-anchoTextoInferior)/2, 460);

        return memeImage;
    }
}
