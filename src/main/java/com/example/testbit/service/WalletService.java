package com.example.testbit.service;
import com.example.testbit.model.WalletEntity;
import com.example.testbit.repository.WalletRepository;
import jakarta.websocket.SendResult;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository; // Repository for PostgreSQL

    private final PeerGroup peerGroup;


    private final BlockStore blockStore;

    public WalletService(WalletRepository walletRepository, PeerGroup peerGroup, BlockStore blockStore) {
        this.walletRepository = walletRepository;
        this.peerGroup = peerGroup;
        this.blockStore = blockStore;
    }


    public Wallet createWallet(String userId) throws IOException {
        NetworkParameters params = TestNet3Params.get();
        Wallet wallet = Wallet.createDeterministic(params, Script.ScriptType.P2PKH);

        // ذخیره‌سازی کیف‌پول در فایل
        File walletFile = new File(userId + "_wallet.wallet");
        wallet.saveToFile(walletFile);

        Address address = wallet.currentReceiveAddress();
        System.out.println("آدرس کیف پول: " + address);
        // ذخیره اطلاعات کیف‌پول در دیتابیس
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setUserId(userId);
        walletEntity.setFilePath(walletFile.getPath());
        walletEntity.setBalance(wallet.getBalance());
        walletEntity.setAddress(address.toString());
        walletRepository.save(walletEntity);

        return wallet;
    }


    public Coin getBalance(String userId) throws UnreadableWalletException {
        WalletEntity walletEntity = walletRepository.findByUserId(userId);
        if (walletEntity != null) {
            Wallet wallet = Wallet.loadFromFile(new File(walletEntity.getFilePath()));
            addWalletListener(wallet, userId);
            // همگام‌سازی کیف‌پول با بلاک‌چین
            peerGroup.addWallet(wallet);
            peerGroup.downloadBlockChain();
            return wallet.getBalance();
        }
        return Coin.ZERO; // اگر کیف‌پول وجود نداشته باشد
    }

    public String sendTransaction(String userId, String toAddress, String amount) throws Exception {
        WalletEntity walletEntity = walletRepository.findByUserId(userId);
        if (walletEntity != null) {
            Wallet wallet = Wallet.loadFromFile(new File(walletEntity.getFilePath()));
            Coin coinAmount = Coin.parseCoin(amount);
            Address address = Address.fromString(wallet.getNetworkParameters(), toAddress);

            SendRequest sendRequest = SendRequest.to(address, coinAmount);
            Wallet.SendResult sendResult = wallet.sendCoins(peerGroup, sendRequest); // peerGroup قبلاً پیکربندی شده است

            return sendResult.tx.getTxId().toString(); // برگرداندن TXID
        }
        throw new Exception("Wallet not found.");
    }

    public Transaction getTransaction(String txId) throws Exception {
        Sha256Hash txHash = Sha256Hash.wrap(txId);
        List<Peer> peers = peerGroup.getConnectedPeers();

        for (Peer peer : peers) {
            try {
                // درخواست بلاک با استفاده از ارتفاع
                Block block = blockStore.getChainHead().getHeader();
                if (block != null) {
                    for (Transaction transaction : block.getTransactions()) {
                        if (transaction.getTxId().equals(txHash)) {
                            return transaction; // اطلاعات تراکنش
                        }
                    }
                }
            } catch (Exception e) {
                // Log error or handle it as needed
            }
        }
        throw new Exception("Transaction not found.");
    }

    private void addWalletListener(Wallet wallet, String userId) {
        wallet.addCoinsReceivedEventListener((w, tx, prevBalance, newBalance) -> {
            System.out.println("Received transaction for wallet: " + tx.getTxId());
            updateBalance(userId, newBalance);
        });

        wallet.addCoinsSentEventListener((w, tx, prevBalance, newBalance) -> {
            System.out.println("Sent transaction from wallet: " + tx.getTxId());
            updateBalance(userId, newBalance);
        });
    }

    private void updateBalance(String userId, Coin newBalance) {
        WalletEntity walletEntity = walletRepository.findByUserId(userId);
        if (walletEntity != null) {
            walletEntity.setBalance(newBalance);
            walletRepository.save(walletEntity);
        }
    }

}


