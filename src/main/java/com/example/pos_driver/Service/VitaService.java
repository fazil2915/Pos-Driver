package com.example.pos_driver.Service;

import com.example.pos_driver.Model.Hsm;
import com.example.pos_driver.Model.Key;
import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VitaService {

    @Autowired
    private TerminalRepo terminalRepo;
    @Autowired
    private MerchantRepo merchantRepo;
    @Autowired
    private FitRepo fitRepo;
    @Autowired
    private DeviceManagerRepo deviceManagerRepo;
    @Autowired
    private KeyRepo keyRepo;
    @Autowired
    private HsmRepo hsmRepo;


    public Optional<Terminal> findTerminalBySerialNumber(String serialNumber){

        return terminalRepo.findBySerialNumber(serialNumber);
    }


}
