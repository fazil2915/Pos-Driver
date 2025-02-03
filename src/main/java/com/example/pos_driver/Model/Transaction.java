package com.example.pos_driver.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {


    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;
    @Column(name = "card_type", length = 50)
    private String cardType;

    @Column(name = "slno", length = 50)
    private String slno;

    @Column(name = "ksn", length = 50)
    private String ksn;

    @Column(name = "track2", length = 50)
    private String track2;

    @Column(name = "pin", length = 50)
    private String pin;

    @Column(name = "pan", length = 50)
    private String pan;

    @Column(name = "processing_code", length = 50)
    private String processingCode;

    @Column(name = "amount", length = 50)
    private String amount;

    @Column(name = "transaction_date_time", length = 50)
    private String transactionDateTime;

    @Column(name = "stan", length = 50)
    private String stan;

    @Column(name = "time", length = 50)
    private String time;

    @Column(name = "date", length = 50)
    private String date;

    @Column(name = "date_expiration", length = 50)
    private String dateExpiration;

    @Column(name = "pos_entry_mode", length = 50)
    private String posEntryMode;

    @Column(name = "pos_condition_code", length = 50)
    private String posConditionCode;

    @Column(name = "card_acceptor_terminal_id", length = 50)
    private String cardAcceptorTerminalId;

    @Column(name = "card_acceptor_id_code", length = 50)
    private String cardAcceptorIdCode;

    @Column(name = "card_acceptor_name_location")
    private String cardAcceptorNameLocation;

    @Column(name = "currency_code", length = 50)
    private String currencyCode;

    @Column(name = "receiving_institution_id_code", length = 50)
    private String receivingInstitutionIdCode;

    @Column(name = "pos_data_code", length = 50)
    private String posDataCode;

    @Column(name = "switch_key", length = 50)
    private String switchKey;

    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Column(name = "routing_info", length = 50)
    private String routingInfo;

    @Column(name = "auth_date_set", length = 50)
    private String authDateSet;

    @Column(name = "amt_settle", length = 50)
    private String amtSettle;

    @Column(name = "amt_tran_fee", length = 50)
    private String amtTranFee;

    @Column(name = "amt_proc_fee", length = 50)
    private String amtProcFee;

    @Column(name = "amt_settle_fee", length = 50)
    private String amtSettleFee;

    @Column(name = "amt_proc_fee_tran", length = 50)
    private String amtProcFeeTran;

    @Column(name = "amt_proc_fee_settle", length = 50)
    private String amtProcFeeSettle;

    @Column(name = "echo_data", length = 50)
    private String echoData;

    @Column(name = "msg_reason_code", length = 50)
    private String msgReasonCode;

    @Column(name = "pos_pin_capture_code", length = 50)
    private String posPinCaptureCode;

    @Column(name = "acquiring_institution_id_code", length = 50)
    private String acquiringInstitutionIdCode;

    @Column(name = "pos_data", length = 50)
    private String posData;

    @Column(name = "auth_profile", length = 50)
    private String authProfile;

    @Column(name = "pos_geo_data", length = 50)
    private String posGeoData;

    @Column(name = "sponsor_bank", length = 50)
    private String sponsorBank;

    @Column(name = "resp_code", length = 2)
    private String respCode;

    @Column(name = "icc_data", columnDefinition = "TEXT")
    private String iccData;

    @Column(name ="msg_type")
    private  String msgType;


}
