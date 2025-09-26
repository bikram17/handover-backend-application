package com.arenabast.api.entity;

import com.arenabast.api.entity.base.WalletBalanceBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Entity
@Table
@Getter
@Setter
@FieldNameConstants
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AgentWalletBalance extends WalletBalanceBaseEntity {

}
