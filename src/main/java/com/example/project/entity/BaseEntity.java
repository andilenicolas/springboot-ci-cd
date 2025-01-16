package com.example.project.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import com.example.project.converters.UTCDateTimeConverter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity 
{
	 @Id
	 @UuidGenerator
	 @Column(name = "id", updatable = false, nullable = false)
	 private UUID id;
	 
	 @LastModifiedDate
	 @Column(name = "updated_at")
	 @Convert(converter = UTCDateTimeConverter.class)
	 private LocalDateTime updatedAt;
	
	@Column(name = "deleted_at")
    @Convert(converter = UTCDateTimeConverter.class)
    private LocalDateTime deletedAt;
    
	 @CreatedDate
	 @Column(name = "created_at", nullable = false, updatable = false)
	 @Convert(converter = UTCDateTimeConverter.class)
	 private LocalDateTime createdAt;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;
    

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deleted;
    }
}

