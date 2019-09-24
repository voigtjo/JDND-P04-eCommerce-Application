package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PathVariable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserControllerTest {
    private static final Logger log = LoggerFactory.getLogger(UserControllerTest.class);

    private UserController userController;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CartRepository cartRepo;

//    private UserRepository userRepo = mock(UserRepository.class);
//
//    private CartRepository cartRepo = mock(CartRepository.class);

    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    @Before
    public void setup(){
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);

    }

    private ResponseEntity<User> createUserResponse(String password) throws Exception {
        return createUserResponse("test", password, password);
    }

    private ResponseEntity<User> createUserResponse(String password, String confirmPassword) throws Exception {
        return createUserResponse("test", password, confirmPassword);
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

    @Test
    public void createUser_happyPath() throws Exception{
        final ResponseEntity<User> response = createUserResponse("testPassword", "testPassword");
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        User u = response.getBody();
        assertNotNull(u);
        log.info("userId= " + u.getId(), "username= " + u.getUsername());
        assertEquals("test",u.getUsername());
        assertEquals("testPassword", u.getPassword());
    }

    @Test
    public void createUser_failure_wrongPwd() throws Exception {
        final ResponseEntity<User> response = createUserResponse("testPassword", "abcdefg");
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createUser_failure_pwdInvalid() throws Exception {
        final ResponseEntity<User> response = createUserResponse("abc", "abc");
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void findById() throws Exception {
        final ResponseEntity<User> createResponse = createUserResponse("testPassword");
        User u = createResponse.getBody();

        ResponseEntity<User> response = userController.findById(u.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(u.getUsername(), response.getBody().getUsername());

        response = userController.findById(100L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void findByUserName() throws Exception {
        final ResponseEntity<User> createResponse = createUserResponse("testPassword");
        User u = createResponse.getBody();

        ResponseEntity<User> response = userController.findByUserName(u.getUsername());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(u.getId(), response.getBody().getId());

        response = userController.findByUserName("XXX");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
