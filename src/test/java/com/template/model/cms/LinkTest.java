package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkTest {
    private Link link;
    
    @BeforeEach
    void setUp() {
        link = new Link();
    }
    
    @Test
    void testLinkProperties() {
        Sys sys = new Sys();
        sys.setType("Link");
        sys.setId("123");
        sys.setLinkType("Entry");
        
        link.setSys(sys);
        
        assertNotNull(link.getSys());
        assertEquals("Link", link.getSys().getType());
        assertEquals("123", link.getSys().getId());
        assertEquals("Entry", link.getSys().getLinkType());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Link link1 = new Link();
        Sys sys1 = new Sys();
        sys1.setId("123");
        link1.setSys(sys1);
        
        Link link2 = new Link();
        Sys sys2 = new Sys();
        sys2.setId("123");
        link2.setSys(sys2);
        
        assertEquals(link1, link2);
        assertEquals(link1.hashCode(), link2.hashCode());
    }
} 