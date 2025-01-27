package com.example.pos_driver.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.SplittableRandom;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "terminals")
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO )
    @Column(name = "id",  unique = true)
    private UUID id;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "terminal_name", nullable = false)
    private String terminalName;

    @Column(name = "terminal_type", nullable = false)
    private String terminalType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "date_deployed")
    private LocalDateTime dateDeployed;



    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "kwp_key", nullable = false)
    private String kwpKeyName;

    @Column(name = "key_length", nullable = false)
    private String keyLength;

    @Column(name = "parent", nullable = true)
    private String parent;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "sponsor_bank", referencedColumnName = "id", nullable = true)
    private   FinancialInstitution  sponsorBank;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "device", referencedColumnName = "device_id", nullable = true)
    private DeviceManager device;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "merchant", referencedColumnName = "id", nullable = true)
    private Merchant merchant;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "key", referencedColumnName = "id", nullable = true)
    private Key key;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "switch", referencedColumnName = "id", nullable = true)
    private Switch Switch;

    @ManyToOne( cascade = CascadeType.REMOVE)
    @JoinColumn(name = "hsm", referencedColumnName = "id", nullable = true)
    private Hsm hsm ;

}
