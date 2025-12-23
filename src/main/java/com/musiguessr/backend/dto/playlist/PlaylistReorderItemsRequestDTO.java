package com.musiguessr.backend.dto.playlist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistReorderItemsRequestDTO {

    @Valid
    @NotEmpty
    private List<PlaylistItemRequestDTO> items;
}