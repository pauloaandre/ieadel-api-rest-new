package ex.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidacaoDTO(
    @NotBlank @Email String email,
    @NotBlank String codigo
) {
}
