package com.example.testbit.controller;

import com.example.testbit.model.WalletDTO;
import com.example.testbit.service.WalletService;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create")
    public WalletDTO createWallet(@RequestParam String userId) throws IOException {
        Wallet wallet = walletService.createWallet(userId);

        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setBalance(wallet.getBalance().toFriendlyString());
        walletDTO.setFilePath(userId + "_wallet.wallet");
        walletDTO.setUserId(userId);
        return walletDTO;
    }

    @GetMapping("/{userId}/balance")
    public Coin getBalance(@PathVariable String userId) throws UnreadableWalletException {
        return walletService.getBalance(userId);
    }

    @PostMapping("/{userId}/send")
    public String sendTransaction(@PathVariable String userId, @RequestParam String toAddress, @RequestParam String amount) throws Exception {
        return walletService.sendTransaction(userId, toAddress, amount);
    }

    @GetMapping("/transaction/{txId}")
    public Transaction getTransaction(@PathVariable String txId) throws Exception {
        return walletService.getTransaction(txId);
    }
}

