package com.example.pos_driver.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hsms")
public class Hsm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    @Column(name = "id",  unique = true)
    private UUID id;

    @Column(name = "hsm_id", nullable = false)
    private String hsmId;

    @Column(name = "hsm_type")
    private String hsmType;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private String port;

    @Column(name = "tls")
    private String tls;

    @Column(name = "status")
    private String status;


}
