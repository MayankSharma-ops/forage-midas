package com.jpmc.midascore.controller;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

    @Autowired
    private DatabaseConduit databaseConduit;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam(name = "userId") Long userId) {
        // Query the database for the user
        UserRecord user = databaseConduit.findById(userId);

        // If the user exists, return their balance. Otherwise, return 0.
        if (user != null) {
            return new Balance(user.getBalance());
        } else {
            return new Balance(0f);
        }
    }
}