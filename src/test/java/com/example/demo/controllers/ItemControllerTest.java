package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ItemControllerTest {
    private static final Logger log = LoggerFactory.getLogger(ItemControllerTest.class);

    private ItemController itemController;

    @Autowired
    private ItemRepository itemRepo;



    @Before
    public void setUp() {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepo);
    }

    @Test
    public void getItems() {
        ResponseEntity<List<Item>> response = itemController.getItems();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<Item> items = response.getBody();
        for (Item item :items){
            log.info("### " + item.getId() + ": " + item.toString());
        }
        assertEquals(2, items.size());
    }

    @Test
    public void getItemById() {
        ResponseEntity<Item> responseItem = itemController.getItemById(1L);
        assertEquals(HttpStatus.OK, responseItem.getStatusCode());
        Item item = responseItem.getBody();
        assertEquals(1L, item.getId().longValue());

        responseItem = itemController.getItemById(3L);
        assertEquals(HttpStatus.NOT_FOUND, responseItem.getStatusCode());
    }

    @Test
    public void getItemsByName() {
        ResponseEntity<List<Item>>response = itemController.getItemsByName("Round Widget");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Round Widget", response.getBody().get(0).getName());

        response = itemController.getItemsByName("Edge Widget");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
