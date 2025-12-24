package com.musiguessr.backend.dto.playlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PlaylistRandomRequestDTO {

    @NotBlank()
    private String name;

    @Min(1)
    private Integer length;

    private PlaylistFilterCriteria criteria;

    @Data
    public static class PlaylistFilterCriteria {
        private List<Long> genres;
        private List<Long> artists;
    }
}