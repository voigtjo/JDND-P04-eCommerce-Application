package com.example.demo.controllers;

import java.util.List;

import com.example.demo.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {
	private static final Logger log = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	
	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		UserOrder order;
		try{
			User user = userRepository.findByUsername(username);
			if (user == null) {
				String message = "|order_request_failures| user= " + username;
				log.error(message);
				throw new UserNotFoundException(message);
			}
			order = UserOrder.createFromCart(user.getCart());
			orderRepository.save(order);

			log.info("|order_request_successes| user= " + username);
		} catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(order);
	}
	
	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		try {
			if (user == null) {
				String message = "|order_history_failures| user= " + username;
				log.error(message);
				throw new UserNotFoundException(message);
			}
		} catch(Exception e){
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(orderRepository.findByUser(user));
	}
}
