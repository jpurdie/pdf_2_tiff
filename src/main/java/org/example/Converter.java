package org.example;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter {

  private static final Logger logger = LoggerFactory.getLogger(Converter.class);

    public static byte[] readBytesFromDisk(String source) throws IOException {
      Path myPath = Paths.get(source);
      if(!Files.exists(myPath)){
          throw new IOException(source + " doesn\'t exist");
      }
      return Files.readAllBytes(myPath);
  }


  public static byte[] toByteArray(BufferedImage bi, String format)
          throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bi, format, baos);
    byte[] bytes = baos.toByteArray();
    return bytes;

  }

  public static File convertPdf(byte[] sourceBytes)
      throws IOException, PDFSecurityException, PDFException, InterruptedException {

    File tempTiffFile = File.createTempFile("temp.", ".tiff");
    tempTiffFile.deleteOnExit();

    Document document = new Document();
    document.setByteArray(sourceBytes, 0, sourceBytes.length, "");

     ImageOutputStream ios = ImageIO.createImageOutputStream(tempTiffFile);
//    ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
//    writer.setOutput(ios);

    float scale = 1.0f;
    float rotation = 0f;

    BufferedImage[] images = new BufferedImage[document.getNumberOfPages()];

    for (int i = 0; i < document.getNumberOfPages(); i++) {
      BufferedImage image =
          (BufferedImage)
              document.getPageImage(
                  i, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX, rotation, scale);
      images[i] = image;
      image.flush();
    }
    document.dispose();


    // Obtain a TIFF writer
    ImageWriter writer = ImageIO.getImageWritersByFormatName("TIFF").next();
    OutputStream stream = new FileOutputStream(tempTiffFile);

    try (ImageOutputStream output = ImageIO.createImageOutputStream(stream)) {
      writer.setOutput(output);

      ImageWriteParam params = writer.getDefaultWriteParam();
      params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

      // Compression: None, PackBits, ZLib, Deflate, LZW, JPEG and CCITT variants allowed
      // (different plugins may use a different set of compression type names)
      params.setCompressionType("LZW");

      writer.prepareWriteSequence(null);

      for (BufferedImage image : images) {
        writer.writeToSequence(new IIOImage(image, null, null), params);
      }

      // We're done
      writer.endWriteSequence();
    }

    writer.dispose();


    return tempTiffFile;
  }
}
