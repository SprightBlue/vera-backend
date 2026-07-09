package com.unlam.verabackend.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader; 

    @Mock
    private MultipartFile image;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    void deberiaSubirImagenYRetornarUrlConExito() throws Exception {

        String folder = "perfiles";
        byte[] imageBytes = "bytes-falsos-de-imagen".getBytes();
        String urlEsperada = "https://res.cloudinary.com/demo/image/upload/v1234/perfiles/foto.jpg";

        when(image.getBytes()).thenReturn(imageBytes);
        
        when(cloudinary.uploader()).thenReturn(uploader);
        
        when(uploader.upload(eq(imageBytes), any(Map.class))).thenReturn(Map.of("secure_url", urlEsperada));

        String urlResultado = cloudinaryService.uploadImage(image, folder);

        assertEquals(urlEsperada, urlResultado);
        verify(uploader, times(1)).upload(eq(imageBytes), any(Map.class));
    }

    @Test
    void deberiaLanzarExcepcionSiFallaAlLeerLosBytesDeLaImagen() throws IOException {
        String folder = "perfiles";
        
        when(image.getBytes()).thenThrow(new IOException("Error de lectura del archivo"));

        IOException exception = assertThrows(IOException.class, () -> {
            cloudinaryService.uploadImage(image, folder);
        });

        assertEquals("Error de lectura del archivo", exception.getMessage());
        
    }
}