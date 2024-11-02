package com.example.testbit.config;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.wallet.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BitcoinConfig {

    @Bean
    public NetworkParameters networkParameters() {
        return TestNet3Params.get();
    }

    @Bean
    public BlockStore blockStore(NetworkParameters params) throws Exception {
        return new MemoryBlockStore(params);
    }

    @Bean
    public Wallet wallet(NetworkParameters params) {
        return new Wallet(params);
    }

    @Bean
    public BlockChain blockChain(NetworkParameters params, Wallet wallet, BlockStore blockStore) throws Exception {
        return new BlockChain(params, wallet, blockStore);
    }

    @Bean
    public PeerGroup peerGroup(NetworkParameters params, BlockChain blockChain) throws Exception {
        PeerGroup peerGroup = new PeerGroup(params, blockChain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.start();
        return peerGroup;
    }
}


