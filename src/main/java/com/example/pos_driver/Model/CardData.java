package com.example.pos_driver.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Table;

@Entity
@Table(name = "card_data")
@Data  // Lombok annotation to generate getters, setters, toString, etc.
@NoArgsConstructor  // Lombok annotation to generate a no-args constructor
@AllArgsConstructor
public class CardData {
    @Id
    @Column(name = "sl_no")
    private String sl_no;

    @Column(name = "card_type")
    private String card_type;

    @Column(name = "ksn")
    private String ksn;

    @Column(name = "track2")
    private String track2;

    @Column(name = "pin")
    private String pin;

    @Column(name = "pan")
    private String pan;

    @Column(name = "processing_code")
    private String processing_code;

    @Column(name = "amount")
    private String amount;

    @Column(name = "transaction_date_time")
    private String transaction_date_time;

    @Column(name = "stan")
    private String stan;

    @Column(name = "time")
    private String time;

    @Column(name = "date")
    private String date;

    @Column(name = "date_expiration")
    private String date_expiration;

    @Column(name = "pos_entry_mode")
    private String pos_entry_mode;

    @Column(name = "pos_condition_code")
    private String pos_condition_code;

    @Column(name = "card_acceptor_terminal_id")
    private String card_acceptor_terminal_id;

    @Column(name = "card_acceptor_id_code")
    private String card_acceptor_id_code;

    @Column(name = "card_acceptor_name_location")
    private String card_acceptor_name_location;

    @Column(name = "currency_code")
    private String currency_code;

    @Column(name = "receiving_institution_id_code")
    private String receiving_institution_id_code;

    @Column(name = "pos_data_code")
    private String pos_data_code;

    @Column(name = "switch_key")
    private String switch_key;

    @Column(name = "service_code")
    private String service_code;

    @Column(name = "routing_info")
    private String routing_info;

    @Column(name = "auth_date_set")
    private String auth_date_set;

    @Column(name = "amt_tran_fee")
    private String amt_tran_fee;

    @Column(name = "amt_proc_fee")
    private String amt_proc_fee;

    @Column(name = "echo_data")
    private String echo_data;

    @Column(name = "msg_reason_code")
    private String msg_reason_code;

    @Column(name = "pos_pin_capture_code")
    private String pos_pin_capture_code;

    @Column(name = "acquiring_institution_id_code")
    private String acquiring_institution_id_code;

    @Column(name = "pos_data")
    private String pos_data;

    @Column(name = "auth_profile")
    private String auth_profile;

    @Column(name = "pos_geo_data")
    private String pos_geo_data;

    @Column(name = "sponsor_bank")
    private String sponsor_bank;

    @Column(name = "card_seq_no")
    private String card_seq_no;

    @Column(name = "concat")
    private boolean concat;

    @Column(name = "white_space_xml")
    private boolean white_space_xml;

    @Column(name = "appendToICC")
    private String appendToICC;

    @Column(name = "icFormatPost")
    private int icFormatPost;

    @Column(name = "ireq_amount_authorized")
    private String ireq_amount_authorized;

    @Column(name = "ireq_amount_other")
    private String ireq_amount_other;

    @Column(name = "ireq_application_identifier")
    private String ireq_application_identifier;

    @Column(name = "ireq_application_interchange_profile")
    private String ireq_application_interchange_profile;

    @Column(name = "ireq_application_transaction_counter")
    private String ireq_application_transaction_counter;

    @Column(name = "ireq_application_usage_control")
    private String ireq_application_usage_control;

    @Column(name = "ireq_authorization_response_code")
    private String ireq_authorization_response_code;

    @Column(name = "ireq_card_authentication_reliability_indicator")
    private String ireq_card_authentication_reliability_indicator;

    @Column(name = "ireq_card_authentication_results_code")
    private String ireq_card_authentication_results_code;

    @Column(name = "ireq_chip_condition_code")
    private String ireq_chip_condition_code;

    @Column(name = "ireq_cryptogram")
    private String ireq_cryptogram;

    @Column(name = "ireq_cry_info_data")
    private String ireq_cry_info_data;

    @Column(name = "ireq_customer_exc_data")
    private String ireq_customer_exc_data;

    @Column(name = "ireq_cvm_list")
    private String ireq_cvm_list;

    @Column(name = "ireq_cvm_results")
    private String ireq_cvm_results;

    @Column(name = "ireq_form_factor_indicator")
    private String ireq_form_factor_indicator;

    @Column(name = "ireq_interface_device_serial_number")
    private String ireq_interface_device_serial_number;

    @Column(name = "iac_default")
    private String iac_default;

    @Column(name = "iac_denial")
    private String iac_denial;

    @Column(name = "iac_online")
    private String iac_online;

    @Column(name = "ireq_issuer_app_data")
    private String ireq_issuer_app_data;

    @Column(name = "ireq_script_results")
    private String ireq_script_results;

    @Column(name = "ireq_terminal_app_version_number")
    private String ireq_terminal_app_version_number;

    @Column(name = "ireq_terminal_capabilities")
    private String ireq_terminal_capabilities;

    @Column(name = "ireq_terminal_country_Code")
    private String ireq_terminal_country_Code;

    @Column(name = "ireq_terminal_type")
    private String ireq_terminal_type;

    @Column(name = "ireq_terminal_verification_result")
    private String ireq_terminal_verification_result;

    @Column(name = "ireq_transaction_category_code")
    private String ireq_transaction_category_code;

    @Column(name = "ireq_transaction_currency_code")
    private String ireq_transaction_currency_code;

    @Column(name = "ireq_transaction_date")
    private String ireq_transaction_date;

    @Column(name = "ireq_transaction_sequence_counter")
    private String ireq_transaction_sequence_counter;

    @Column(name = "ireq_transaction_type")
    private String ireq_transaction_type;

    @Column(name = "ireq_unpredictable_number")
    private String ireq_unpredictable_number;

    @Column(name = "resp_code")
    private String resp_code;

    @Column(name = "iresp_application_transaction_counter")
    private String iresp_application_transaction_counter;

    @Column(name = "iresp_card_authentication_results_code")
    private String iresp_card_authentication_results_code;

    @Column(name = "iresp_issuer_authentication_data")
    private String iresp_issuer_authentication_data;

    @Column(name = "iresp__issuer_script_template1")
    private String iresp__issuer_script_template1;

    @Column(name = "iresp_issuer_script_template2")
    private String iresp_issuer_script_template2;

    @Column(name = "iccDataString1")
    private String iccDataString1;
}
