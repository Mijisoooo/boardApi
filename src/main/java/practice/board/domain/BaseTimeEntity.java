package practice.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseTimeEntity {

    @CreatedDate
    @NotNull
    @Column(updatable = false, name = "created_date")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime modifiedAt;
}
