package com.example.pos_driver.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "keys")
public class Key {



    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(name = "key_name", nullable = false, unique = true, length = 15)
    private String keyName;

    @Column(name = "value", nullable = false, unique = true, length = 15)
    private String value;

    @Column(name = "check_digit", nullable = true)
    private String checkDigit;

    @Column(name = "port", nullable = true)
    private String port;

    @Column(name = "type", nullable = true)
    private String type;

    @Column(name = "length", nullable = true)
    private String length;

    @Column(name = "domain", nullable = true)
    private String domain;

    @Column(name = "parent", nullable = true)
    private String parent;

    @Column(name = "value_under_parent", nullable = true)
    private String valueUnderParent;

}
