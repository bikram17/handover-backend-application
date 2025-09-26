package com.arenabast.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity extends BaseFieldsEntity {
    @Column(nullable = false)
    String name;
    @Column
    String userName;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    @JsonIgnore
    String passwordHash;
    String createdBy;
    String updatedBy;
    boolean active;
}
