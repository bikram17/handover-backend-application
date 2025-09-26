package com.arenabast.api.controller;


import com.arenabast.api.auth.jwt.JwtUtil;
import com.arenabast.api.dao.PlayerDao;
import com.arenabast.api.dao.WalletDao;
import com.arenabast.api.dto.LoginRequestDto;
import com.arenabast.api.dto.LoginResponseDto;
import com.arenabast.api.dto.PlayerResponseDto;
import com.arenabast.api.dto.WalletBalanceDto;
import com.arenabast.api.entity.PlayerEntity;
import com.arenabast.api.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player")
@RequiredArgsConstructor
public class ArenabastLoginController {

    private final PlayerDao playerDao;
    private final WalletDao walletDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        PlayerEntity player = playerDao.findByUserName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(player.getUserName(), "PLAYER");

        WalletEntity wallet = walletDao.findByPlayerId(player.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        WalletBalanceDto walletDto = new WalletBalanceDto();
        walletDto.setActualBalance(wallet.getCashBalance());
        walletDto.setVirtualBalance(wallet.getVirtualBalance());

        PlayerResponseDto playerDto = new PlayerResponseDto();
        playerDto.setId(player.getId());
        playerDto.setName(player.getName());
        playerDto.setUsername(player.getUserName());
        playerDto.setWallet(walletDto);

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setPlayer(playerDto);

        return ResponseEntity.ok(response);
    }
}
