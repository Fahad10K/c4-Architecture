package com.pizza.delivery.service;

import com.pizza.delivery.dto.CartDTO;
import com.pizza.delivery.dto.CartItemDTO;
import com.pizza.delivery.entity.*;
import com.pizza.delivery.exception.BadRequestException;
import com.pizza.delivery.exception.ResourceNotFoundException;
import com.pizza.delivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;

    public CartDTO getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return CartDTO.builder().items(new ArrayList<>())
                    .subtotal(0.0).tax(0.0).deliveryFee(0.0).discount(0.0).total(0.0).build();
        }
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO addItem(String userId, CartItemDTO itemDTO) {
        MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", itemDTO.getMenuItemId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        if (cart == null) {
            Store store = menuItem.getStore();
            cart = Cart.builder().user(user).store(store).build();
            cart = cartRepository.save(cart);
        } else if (cart.getStore() != null && !cart.getStore().getId().equals(menuItem.getStore().getId())) {
            throw new BadRequestException("Cannot add items from different stores. Clear cart first.");
        }

        int qty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1;
        double unitPrice = menuItem.getPrice();

        CartItem cartItem = CartItem.builder()
                .cart(cart).menuItem(menuItem)
                .quantity(qty).unitPrice(unitPrice).totalPrice(unitPrice * qty)
                .customizations(itemDTO.getCustomizations() != null ? itemDTO.getCustomizations() : "[]")
                .specialNotes(itemDTO.getSpecialNotes())
                .build();

        cartItemRepository.save(cartItem);
        cart = cartRepository.findByUserId(userId).orElseThrow();
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO updateItem(String userId, String itemId, CartItemDTO itemDTO) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        if (itemDTO.getQuantity() != null && itemDTO.getQuantity() > 0) {
            item.setQuantity(itemDTO.getQuantity());
            item.setTotalPrice(item.getUnitPrice() * itemDTO.getQuantity());
        }
        if (itemDTO.getCustomizations() != null) item.setCustomizations(itemDTO.getCustomizations());
        if (itemDTO.getSpecialNotes() != null) item.setSpecialNotes(itemDTO.getSpecialNotes());
        cartItemRepository.save(item);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow();
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO removeItem(String userId, String itemId) {
        cartItemRepository.deleteById(itemId);
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null && cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return CartDTO.builder().items(new ArrayList<>())
                    .subtotal(0.0).tax(0.0).deliveryFee(0.0).discount(0.0).total(0.0).build();
        }
        return cart != null ? mapToDTO(cart) : CartDTO.builder().items(new ArrayList<>())
                .subtotal(0.0).tax(0.0).deliveryFee(0.0).discount(0.0).total(0.0).build();
    }

    @Transactional
    public CartDTO clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
        return CartDTO.builder().items(new ArrayList<>())
                .subtotal(0.0).tax(0.0).deliveryFee(0.0).discount(0.0).total(0.0).build();
    }

    @Transactional
    public CartDTO applyCoupon(String userId, String couponCode) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));

        if (!coupon.getIsActive()) throw new BadRequestException("Coupon is expired");

        double subtotal = cart.getItems().stream().mapToDouble(CartItem::getTotalPrice).sum();
        if (subtotal < coupon.getMinOrderAmount()) {
            throw new BadRequestException("Minimum order amount of $" + coupon.getMinOrderAmount() + " required");
        }

        double discount = 0;
        if ("percentage".equals(coupon.getDiscountType())) {
            discount = subtotal * coupon.getDiscountValue() / 100;
            if (coupon.getMaxDiscount() != null) discount = Math.min(discount, coupon.getMaxDiscount());
        } else {
            discount = coupon.getDiscountValue();
        }

        cart.setCouponCode(couponCode);
        cart.setDiscount(discount);
        cartRepository.save(cart);
        return mapToDTO(cart);
    }

    private CartDTO mapToDTO(Cart cart) {
        double subtotal = cart.getItems().stream().mapToDouble(CartItem::getTotalPrice).sum();
        double tax = subtotal * 0.08;
        double deliveryFee = cart.getStore() != null ? cart.getStore().getDeliveryFee() : 0;
        double discount = cart.getDiscount() != null ? cart.getDiscount() : 0;
        double total = subtotal + tax + deliveryFee - discount;

        return CartDTO.builder()
                .id(cart.getId())
                .storeId(cart.getStore() != null ? cart.getStore().getId() : null)
                .storeName(cart.getStore() != null ? cart.getStore().getName() : null)
                .items(cart.getItems().stream().map(ci -> CartItemDTO.builder()
                        .id(ci.getId()).menuItemId(ci.getMenuItem().getId())
                        .menuItemName(ci.getMenuItem().getName())
                        .menuItemImage(ci.getMenuItem().getImageUrl())
                        .quantity(ci.getQuantity()).customizations(ci.getCustomizations())
                        .unitPrice(ci.getUnitPrice()).totalPrice(ci.getTotalPrice())
                        .specialNotes(ci.getSpecialNotes()).build())
                        .collect(Collectors.toList()))
                .couponCode(cart.getCouponCode())
                .subtotal(Math.round(subtotal * 100.0) / 100.0)
                .tax(Math.round(tax * 100.0) / 100.0)
                .deliveryFee(deliveryFee)
                .discount(Math.round(discount * 100.0) / 100.0)
                .total(Math.round(total * 100.0) / 100.0)
                .build();
    }
}
