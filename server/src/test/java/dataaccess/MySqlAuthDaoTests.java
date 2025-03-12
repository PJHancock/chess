package dataaccess;

import dataaccess.sql.MySqlAuthDao;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySqlAuthDaoTests {

    private MySqlAuthDao testDataBase;

    @BeforeEach
    void setUp() throws DataAccessException {
        testDataBase = new MySqlAuthDao();
        testDataBase.clear();
    }

    @Test
    void clear() throws DataAccessException {
        String newAuth = testDataBase.generateToken("testUser");
        assertTrue(testDataBase.getAuth(newAuth));
        testDataBase.clear();
        assertFalse(testDataBase.getAuth(newAuth));
    }

    @Test
    void generateTokenPositive() throws DataAccessException {
        String newAuth = testDataBase.generateToken("testUser");
        assertTrue(testDataBase.getAuth(newAuth));
    }

    @Test
    void generateTokenNegative() throws DataAccessException {
        String newAuth = testDataBase.generateToken("testUser");
        assertTrue(testDataBase.getAuth(newAuth));
        assertThrows(DataAccessException.class, () -> {
            testDataBase.generateToken(null);
        }, "Should throw DataAccessException for null request");
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        String newAuth = testDataBase.generateToken("testUser");
        assertTrue(testDataBase.getAuth(newAuth));
        assertFalse(testDataBase.getAuth("otherUser"));
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        String newAuth = testDataBase.generateToken("testUser");
        assertTrue(testDataBase.getAuth(newAuth));
        assertThrows(DataAccessException.class, () -> {
            testDataBase.getAuth(null);
        }, "Should throw DataAccessException for null request");
    }

    @Test
    void deleteAuthPositive() {
    }

    @Test
    void deleteAuthNegative() {
    }

    @Test
    void getUserPositive() {
    }

    @Test
    void getUserNegative() {
    }
}