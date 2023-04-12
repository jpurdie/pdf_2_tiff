package org.example;


import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ConverterTest {

    @Test
    public void When_document_exists_read_bytes() throws IOException {
    byte[] myBytes =
        Converter.readBytesFromDisk(
            "src\\test\\resources\\samplepdf.pdf");
        Assertions.assertTrue(myBytes.length > 0);
    }

    @Test
    public void When_document_exists_convert() throws IOException, PDFSecurityException, PDFException, InterruptedException {
        byte[] myBytes =
                Converter.readBytesFromDisk(
                        "src\\test\\resources\\samplepdf.pdf");

        File tempFile = Converter.convertPdf(myBytes);


        Assertions.assertTrue(tempFile.exists());
    }


}
