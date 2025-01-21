package com.example.pos_driver.Service;

import com.example.pos_driver.Model.Terminal;
import com.example.pos_driver.Repo.DeviceManagerRepo;
import com.example.pos_driver.Repo.FitRepo;
import com.example.pos_driver.Repo.MerchantRepo;
import com.example.pos_driver.Repo.TerminalRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public Optional<Terminal> findTerminalBySerialNumber(String serialNumber){

        return terminalRepo.findBySerialNumber(serialNumber);
    }
}
