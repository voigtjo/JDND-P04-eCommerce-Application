package com.example.demo.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) throws Exception {
		User user = new User();
		try {
			if(!createUserRequest.getPassword().contentEquals(createUserRequest.getConfirmPassword())){
				log.warn("ERROR: password and confirmPassword do not match", createUserRequest.getUsername());
				return ResponseEntity.badRequest().build();
			}
			if (createUserRequest.getPassword().length() < 7 ||
					!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
				log.warn("ERROR: password.length must be >= 7", createUserRequest.getUsername());
				return ResponseEntity.badRequest().build();
			}

			user.setUsername(createUserRequest.getUsername());

			Cart cart = new Cart();
			cartRepository.save(cart);
			user.setCart(cart);

			user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));

			userRepository.save(user);
		} catch (Exception e){
			log.error("ERROR in create user: " + createUserRequest.toString());
			throw new Exception(e);
		}

		log.info("user created: " + user.toString());
		return ResponseEntity.ok(user);
	}
	
}
