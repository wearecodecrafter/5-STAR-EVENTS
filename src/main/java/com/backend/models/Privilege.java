package com.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="privileges")
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID privilegeId;

    private String readPermission;

    private String deletePermission;

    private String updatePermission;

    private String writePermission;

    @OneToOne(mappedBy = "privilege")
    @JsonIgnore
    private Permissions permission;
}
