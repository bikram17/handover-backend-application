package com.arenabast.api.entity;

import com.arenabast.api.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class CustomerEntity extends BaseEntity {
    Long agentId;
}
