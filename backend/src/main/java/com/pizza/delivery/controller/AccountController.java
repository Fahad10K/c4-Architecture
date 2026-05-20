package com.pizza.delivery.controller;

import com.pizza.delivery.dto.AddressDTO;
import com.pizza.delivery.dto.UserDTO;
import com.pizza.delivery.security.UserPrincipal;
import com.pizza.delivery.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(accountService.getProfile(principal.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                  @RequestBody UserDTO dto) {
        return ResponseEntity.ok(accountService.updateProfile(principal.getId(), dto));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(accountService.getAddresses(principal.getId()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> addAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                  @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.addAddress(principal.getId(), dto));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable String addressId,
                                                     @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(accountService.updateAddress(principal.getId(), addressId, dto));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserPrincipal principal,
                                               @PathVariable String addressId) {
        accountService.deleteAddress(principal.getId(), addressId);
        return ResponseEntity.noContent().build();
    }
}
