package com.template.model.cms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TargetTest {
    private Target target;
    
    @BeforeEach
    void setUp() {
        target = new Target();
    }
    
    @Test
    void testTargetProperties() {
        Sys sys = new Sys();
        sys.setType("Target");
        sys.setId("456");
        
        target.setSys(sys);
        
        assertNotNull(target.getSys());
        assertEquals("Target", target.getSys().getType());
        assertEquals("456", target.getSys().getId());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Target target1 = new Target();
        Sys sys1 = new Sys();
        sys1.setId("456");
        target1.setSys(sys1);
        
        Target target2 = new Target();
        Sys sys2 = new Sys();
        sys2.setId("456");
        target2.setSys(sys2);
        
        assertEquals(target1, target2);
        assertEquals(target1.hashCode(), target2.hashCode());
    }
} 