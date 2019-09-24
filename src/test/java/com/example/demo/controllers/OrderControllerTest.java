package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderControllerTest {
    private static final Logger log = LoggerFactory.getLogger(OrderControllerTest.class);

    private OrderController orderController;
    private CartController cartController;
    private UserController userController;
    private ItemController itemController;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ItemRepository itemRepo;

    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    private ResponseEntity<User> createUserResponse(String password) throws Exception {
        return createUserResponse("test", password, password);
    }

    private ResponseEntity<User> createUserResponse(String username, String password, String confirmPassword) throws Exception {
        when(encoder.encode(password)).thenReturn(password);

        CreateUserRequest r = new CreateUserRequest();
        r.setUsername(username);
        r.setPassword(password);
        r.setConfirmPassword(confirmPassword);

        log.info("createUserResponse:" + r);
        final ResponseEntity<User> response = userController.createUser(r);

        return response;
    }
    @Before
    public void setUp() {
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepo);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepo);

        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);

        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);

        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepo);
    }

    @Test
    public void submit_happyPath() throws Exception {
        final ResponseEntity<User> userResponse = createUserResponse("testPassword");
        User u = userResponse.getBody();
        log.info("### 1 ### " +  u.getId() + ": " + u.toString());

        ResponseEntity<List<Item>> itemResponse = itemController.getItems();
        List<Item> items = itemResponse.getBody();
        Item item = items.get(0);
        log.info("### 2 ### " + item.getId() + ": " + item.toString());

        Cart cart = new Cart();
        cart.setUser(u);
        cart.addItem(item);
        u.setCart(cart);
        cartRepo.save(cart);
        log.info("### 3 ### " + cart.getId() + ": " + cart.toString());

        ResponseEntity<UserOrder> response = orderController.submit(u.getUsername());
        UserOrder userOrderSubmit = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("### 4 ### " + userOrderSubmit.getId() + ": " + userOrderSubmit.toString());
        UserOrder userOrderFind = orderRepo.findById(userOrderSubmit.getId()).get();

        assertEquals(u.getUsername(), userOrderFind.getUser().getUsername());
        assertEquals(item.getName(), userOrderFind.getUser().getCart().getItems().get(0).getName());
    }

    @Test
    public void getOrdersForUserSuccess() throws Exception {
        final ResponseEntity<User> userResponse = createUserResponse("testPassword");
        User u = userResponse.getBody();
        log.info("### 1 ### " +  u.getId() + ": " + u.toString());

        ResponseEntity<List<Item>> itemResponse = itemController.getItems();
        List<Item> items = itemResponse.getBody();
        Item item = items.get(0);
        log.info("### 2 ### " + item.getId() + ": " + item.toString());

        Cart cart = new Cart();
        cart.setUser(u);
        cart.addItem(item);
        u.setCart(cart);
        cartRepo.save(cart);
        log.info("### 3 ### " + cart.getId() + ": " + cart.toString());

        ResponseEntity<UserOrder> submitResponse = orderController.submit(u.getUsername());

        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser(u.getUsername());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(u.getUsername(), response.getBody().get(0).getUser().getUsername());
        assertEquals(item.getName(), response.getBody().get(0).getItems().get(0).getName());
    }
}
