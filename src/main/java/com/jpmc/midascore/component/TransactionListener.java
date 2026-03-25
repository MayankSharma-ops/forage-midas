package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
// Import your TransactionRecord here if it's in a different package
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TransactionListener {

    static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    @Autowired
    private DatabaseConduit databaseConduit;

    // Initialize RestTemplate to make external API calls
    private final RestTemplate restTemplate = new RestTemplate();

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: " + transaction.toString());

        UserRecord sender = databaseConduit.findById(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findById(transaction.getRecipientId());

        if (sender != null && recipient != null) {
            if (sender.getBalance() >= transaction.getAmount()) {

                // 1. Post to the Incentive API
                Incentive incentive = restTemplate.postForObject(
                        "http://localhost:8080/incentive",
                        transaction,
                        Incentive.class
                );

                // Extract the amount (default to 0 if something goes wrong)
                float incentiveAmount = (incentive != null) ? incentive.getAmount() : 0f;

                // 2. Do the math (Deduct from sender, Add transaction + incentive to recipient)
                sender.setBalance(sender.getBalance() - transaction.getAmount());
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

                // 3. Save updated balances
                databaseConduit.save(sender);
                databaseConduit.save(recipient);

                // 4. Save the TransactionRecord (Assuming you have a save method for this in DatabaseConduit)
                /* TransactionRecord record = new TransactionRecord();
                record.setSender(sender);
                record.setRecipient(recipient);
                record.setAmount(transaction.getAmount());
                record.setIncentive(incentiveAmount);
                databaseConduit.save(record);
                */
            }
        }
    }
}