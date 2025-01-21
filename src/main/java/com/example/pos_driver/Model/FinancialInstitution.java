package com.example.pos_driver.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table( name = "financial_institutions")
public class FinancialInstitution {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(name = "fit_id", length = 40, nullable = false)
    private String fitId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "address1", length = 255)
    private String address1;

    @Column(name = "address2", length = 255)
    private String address2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "contact_email_id", length = 100)
    private String contactEmailId;

    @Column(name = "date_engaged")
    private LocalDateTime dateEngaged;

    @Column(name = "trn", length = 50)
    private String trn;

    @Column(name = "fit_name", nullable = false)
    private String fitName;

}
