package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta tras la subida exitosa de una imagen de perfil a Cloudinary")
public class UploadImageResponse {

    @Schema(description = "Correo electrónico del usuario dueño de la imagen", example = "juan.perez@ejemplo.com")
    private String email;

    @Schema(description = "URL segura de Cloudinary donde quedó almacenada la imagen", example = "https://res.cloudinary.com/verabackend/image/upload/users/juan_perez.png")
    private String image;
}