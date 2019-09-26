package com.example.demo.controllers;

import java.util.Optional;
import java.util.stream.IntStream;

import com.example.demo.exceptions.ItemNotPresentException;
import com.example.demo.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	private static final Logger log = LoggerFactory.getLogger(CartController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@PostMapping("/addToCart")
	public ResponseEntity<Cart> addToCart(@RequestBody ModifyCartRequest request) {
		User user = userRepository.findByUsername(request.getUsername());
		Cart cart;
		try {
			if (user == null) {
				String message = "|cart_request_failures| user= " + request.getUsername();
				log.error(message);
				throw new UserNotFoundException(message);
			}
			Optional<Item> item = itemRepository.findById(request.getItemId());
			if (!item.isPresent()) {
				String message = "|cart_request_failures| user= " + request.getUsername();
				log.error(message);
				throw new ItemNotPresentException(message);
			}
			cart = user.getCart();
			IntStream.range(0, request.getQuantity())
					.forEach(i -> cart.addItem(item.get()));
			cartRepository.save(cart);
			log.info("|cart_request_successes| cart.id=" + cart.getId());
		} catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(cart);
	}
	
	@PostMapping("/removeFromCart")
	public ResponseEntity<Cart> removeFromCart(@RequestBody ModifyCartRequest request) {
		User user = userRepository.findByUsername(request.getUsername());
		Cart cart;
		try {
			if (user == null) {
				String message = "|cart_request_failures| user= " + request.getUsername();
				log.error(message);
				throw new UserNotFoundException(message);
			}
			Optional<Item> item = itemRepository.findById(request.getItemId());
			if (!item.isPresent()) {
				String message = "|cart_request_failures| item.id= " + request.getItemId();
				log.error(message);
				throw new ItemNotPresentException(message);
			}
			cart = user.getCart();
			IntStream.range(0, request.getQuantity())
					.forEach(i -> cart.removeItem(item.get()));
			cartRepository.save(cart);
			log.info("|cart_request_successes| cart.id= " + cart.getId());
		} catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(cart);
	}
		
}
