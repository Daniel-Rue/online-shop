package ru.kon.onlineshop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "review_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;
}
