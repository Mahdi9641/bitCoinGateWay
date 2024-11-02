package com.example.testbit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bitcoinj.core.Coin;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String filePath;
    private Coin balance; // ذخیره بالانس
    private String address;

    // Getters و Setters
}
