package tools.jackson.databind.objectid;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class ReferentialWithObjectIdTest extends DatabindTestUtil
{
    public static class EmployeeList {
        public AtomicReference<Employee> first;
    }

    @JsonIdentityInfo(property="id", generator=ObjectIdGenerators.PropertyGenerator.class)
    public static class Employee {
        public int id;
        public String name;
        public AtomicReference<Employee> next;

        public Employee next(Employee n) {
            next = new AtomicReference<Employee>(n);
            return this;
        }
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testAtomicWithObjectId() throws Exception
    {
        Employee first = new Employee();
        first.id = 1;
        first.name = "Alice";

        Employee second = new Employee();
        second.id = 2;
        second.name = "Bob";

        first.next(second);
        second.next(first);

        EmployeeList input = new EmployeeList();
        input.first = new AtomicReference<Employee>(first);

        String json = MAPPER.writeValueAsString(input);

        // and back

        EmployeeList result = MAPPER.readValue(json, EmployeeList.class);
        Employee firstB = result.first.get();
        assertNotNull(firstB);
        assertEquals("Alice", firstB.name);
        Employee secondB = firstB.next.get();
        assertNotNull(secondB);
        assertEquals("Bob", secondB.name);
        assertNotNull(secondB.next.get());
        assertSame(firstB, secondB.next.get());
    }
}
