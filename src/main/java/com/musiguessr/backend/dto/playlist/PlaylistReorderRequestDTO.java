package com.musiguessr.backend.dto.playlist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class PlaylistReorderRequestDTO {

    @NotEmpty
    @Valid
    private List<PlaylistReorderItemDTO> items;
}
