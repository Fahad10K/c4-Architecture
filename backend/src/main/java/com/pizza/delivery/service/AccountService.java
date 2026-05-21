package com.pizza.delivery.service;

import com.pizza.delivery.dto.AddressDTO;
import com.pizza.delivery.dto.UserDTO;
import com.pizza.delivery.entity.Address;
import com.pizza.delivery.entity.User;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.AddressRepository;
import com.pizza.delivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserDTO getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapUserToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(String userId, UserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getPreferences() != null) user.setPreferences(dto.getPreferences());

        user = userRepository.save(user);
        return mapUserToDTO(user);
    }

    public List<AddressDTO> getAddresses(String userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapAddressToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(String userId, AddressDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = Address.builder()
                .user(user)
                .label(dto.getLabel() != null ? dto.getLabel() : "Home")
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry() != null ? dto.getCountry() : "US")
                .lat(dto.getLat())
                .lng(dto.getLng())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();

        address = addressRepository.save(address);
        return mapAddressToDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(String userId, String addressId, AddressDTO dto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (dto.getLabel() != null) address.setLabel(dto.getLabel());
        if (dto.getStreet() != null) address.setStreet(dto.getStreet());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getZipCode() != null) address.setZipCode(dto.getZipCode());
        if (dto.getLat() != null) address.setLat(dto.getLat());
        if (dto.getLng() != null) address.setLng(dto.getLng());
        if (dto.getIsDefault() != null) address.setIsDefault(dto.getIsDefault());

        address = addressRepository.save(address);
        return mapAddressToDTO(address);
    }

    @Transactional
    public void deleteAddress(String userId, String addressId) {
        addressRepository.deleteByIdAndUserId(addressId, userId);
    }

    private UserDTO mapUserToDTO(User user) {
        List<AddressDTO> addresses = user.getAddresses() != null
                ? user.getAddresses().stream().map(this::mapAddressToDTO).collect(Collectors.toList())
                : List.of();

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .preferences(user.getPreferences())
                .addresses(addresses)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressDTO mapAddressToDTO(Address addr) {
        return AddressDTO.builder()
                .id(addr.getId())
                .label(addr.getLabel())
                .street(addr.getStreet())
                .city(addr.getCity())
                .state(addr.getState())
                .zipCode(addr.getZipCode())
                .country(addr.getCountry())
                .lat(addr.getLat())
                .lng(addr.getLng())
                .isDefault(addr.getIsDefault())
                .build();
    }
}
