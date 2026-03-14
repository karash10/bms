package com.bms.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Venue extends BaseEntity {

    private String name;

    private String location;

    private Integer totalCapacity;
}