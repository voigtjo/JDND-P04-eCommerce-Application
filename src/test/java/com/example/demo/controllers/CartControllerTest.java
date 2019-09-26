package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@DataJpaTest
public class CartControllerTest {
    private static final Logger log = LoggerFactory.getLogger(CartControllerTest.class);

    private CartController cartController;
    private UserController userController;
    private ItemController itemController;


    @Autowired
    private UserRepository userRepo;

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
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);

        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);

        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepo);
    }



    @Test
    public void addToCart_happyPath() throws Exception {
        final ResponseEntity<User> userResponse = createUserResponse("testPassword");
        User u = userResponse.getBody();

        ResponseEntity<List<Item>> itemResponse = itemController.getItems();
        List<Item> items = itemResponse.getBody();
        Item item = items.get(0);

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(u.getUsername());
        request.setItemId(item.getId());
        int quantity = 3;
        request.setQuantity(quantity);

        ResponseEntity<Cart> response = cartController.addToCart(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        BigDecimal total = item.getPrice().multiply(new BigDecimal(quantity));
        log.info("### totalCosts= " + total);
        assertEquals(total, response.getBody().getTotal());
    }

    @Test
    public void addToCart_failure_userDoesNotExist() throws Exception {
        ResponseEntity<List<Item>> itemResponse = itemController.getItems();
        List<Item> items = itemResponse.getBody();
        Item item = items.get(0);

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("test");
        request.setItemId(item.getId());
        int quantity = 3;
        request.setQuantity(quantity);

        ResponseEntity<Cart> response = cartController.addToCart(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addToCart_failure_itemDoesNotExist() throws Exception {
        final ResponseEntity<User> userResponse = createUserResponse("testPassword");
        User u = userResponse.getBody();

        Item item = new Item("testItem", BigDecimal.valueOf(100L), "testDesc");
        item.setId(3L);

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(u.getUsername());
        request.setItemId(item.getId());
        int quantity = 3;
        request.setQuantity(quantity);

        ResponseEntity<Cart> response = cartController.addToCart(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void removeFromCartSuccess() throws Exception {
        final ResponseEntity<User> userResponse = createUserResponse("testPassword");
        User u = userResponse.getBody();

        ResponseEntity<List<Item>> itemResponse = itemController.getItems();
        List<Item> items = itemResponse.getBody();
        Item item = items.get(0);

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(u.getUsername());
        request.setItemId(item.getId());
        int quantity = 3;
        request.setQuantity(quantity);

        ResponseEntity<Cart> addToCartResponse = cartController.addToCart(request);
        int removeQuantity = 1;
        request.setQuantity(removeQuantity);

        ResponseEntity<Cart> removeFromCartResponse = cartController.removeFromCart(request);
        assertEquals(HttpStatus.OK, removeFromCartResponse.getStatusCode());

        BigDecimal total = item.getPrice().multiply(new BigDecimal(quantity - removeQuantity));
        log.info("### totalCosts= " + total);
        assertEquals(total, removeFromCartResponse.getBody().getTotal());
    }

}