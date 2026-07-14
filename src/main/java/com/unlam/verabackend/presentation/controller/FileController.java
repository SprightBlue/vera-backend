package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
import com.unlam.verabackend.domain.port.in.UserSettingsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.UploadImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Archivos", description = "Endpoints para la subida, actualización y remoción de archivos multimedia vinculados al almacenamiento en la nube")
public class FileController {

    private final UserSettingsUseCase userSettingsUseCase;
    private final TrustContactUseCase trustContactUseCase;

    @PutMapping("/upload-user-image")
    @Operation(
            summary = "Subir o actualizar la foto de perfil del usuario",
            description = "Envía un archivo multimedia multipart para guardarlo en la carpeta personal de Cloudinary y actualizar el enlace de imagen del perfil del usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Imagen cargada en la nube y asignada al usuario correctamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadImageResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "El archivo provisto está corrupto o posee una extensión no permitida", content = @Content),
                    @ApiResponse(responseCode = "502", description = "Fallo en la comunicación externa con el proveedor de almacenamiento (Cloudinary)", content = @Content)
            }
    )
    public ResponseEntity<UploadImageResponse> uploadUserImage(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam("image") @Parameter(description = "Archivo binario de la imagen (JPEG/PNG/WebP)", required = true) MultipartFile image
    ) throws IOException {
        log.info("REST Request: PUT - Iniciando flujo de carga de multimedia de perfil para [{}]", user.getEmail());
        UploadImageResponse updatedUserImage = userSettingsUseCase.uploadUserImage(user.getEmail(), image);
        return ResponseEntity.ok(updatedUserImage);
    }

    @PatchMapping("/upload-protected-person-image")
    @Operation(
            summary = "Subir foto de un contacto protegido de confianza",
            description = "Sube la foto identificativa de un familiar o contacto protegido y retorna la URL pública del archivo de Cloudinary.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Foto del contacto guardada en la nube con éxito", content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "400", description = "Archivo no válido", content = @Content),
                    @ApiResponse(responseCode = "502", description = "Fallo del proveedor Cloudinary", content = @Content)
            }
    )
    public ResponseEntity<String> uploadProtectedPersonImage(
            @RequestParam("image") @Parameter(description = "Archivo binario de la imagen", required = true) MultipartFile image
    ) throws IOException {
        log.info("REST Request: PATCH - Solicitando guardado en la nube de foto para un familiar protegido");
        String imageUrl = trustContactUseCase.uploadProtectedPersonImage(image);
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/delete-user-image")
    @Operation(
            summary = "Eliminar la foto de perfil actual",
            description = "Remueve la imagen física del servidor de Cloudinary vinculada al usuario autenticado y restablece el campo de imagen a null de forma segura.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Foto de perfil removida exitosamente (No Content)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content),
                    @ApiResponse(responseCode = "502", description = "Fallo al solicitar la remoción física al servidor de Cloudinary", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteUserImage(
            @AuthenticationPrincipal @Parameter(hidden = true) User user
    ) throws IOException {
        log.info("REST Request: DELETE - Solicitando eliminación de foto de perfil para el usuario [{}]", user.getEmail());
        userSettingsUseCase.deleteUserImage(user.getEmail());
        return ResponseEntity.noContent().build();
    }
}