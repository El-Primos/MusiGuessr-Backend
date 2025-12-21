package com.musiguessr.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "playlist_items", schema = "musiguessr_schema")
public class PlaylistItem {

    @EmbeddedId
    private PlaylistItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(
            name = "playlist_id",
            foreignKey = @ForeignKey(name = "playlist_items_playlist_id_fkey")
    )
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "music_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "playlist_items_music_id_fkey")
    )
    private Music music;
}
