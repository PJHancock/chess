package dataaccess;

import dataaccess.sql.MySqlUserDao;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDaoTests {

    private MySqlUserDao testDataBase;

    @BeforeEach
    void setUp() throws DataAccessException {
        testDataBase = new MySqlUserDao();
        testDataBase.clear();
    }

    @Test
    void clear() throws DataAccessException {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        testDataBase.createUser(user);
        assertTrue(testDataBase.getUser("testUser"));
        testDataBase.clear();
        assertFalse(testDataBase.getUser("testUser"));
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        testDataBase.createUser(user);
        assertTrue(testDataBase.getUser("testUser"));
        assertFalse(testDataBase.getUser("otherUser"));
    }

    @Test
    void createUserNegative() {
        UserData user = new UserData(null, "testPassword", "testEmail");
        assertThrows(DataAccessException.class, () -> testDataBase.createUser(user),
                "Should throw dataaccess.DataAccessException for null request");
    }

    @Test
    void getUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        testDataBase.createUser(user);
        assertTrue(testDataBase.getUser("testUser"));
    }

    @Test
    void getUserNegative() {
        UserData user = new UserData(null, "testPassword", "testEmail");
        assertThrows(DataAccessException.class, () -> testDataBase.getUser(user.username()),
                "Should throw dataaccess.DataAccessException for null request");
    }

    @Test
    void verifyUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "testPassword", "testEmail");
        testDataBase.createUser(user);
        assertTrue(testDataBase.verifyUser("testUser", "testPassword"));
    }

    @Test
    void verifyUserNegative() {
        UserData user = new UserData(null, "testPassword", "testEmail");
        assertThrows(DataAccessException.class, () -> testDataBase.verifyUser(user.username(), user.password()),
                "Should throw dataaccess.DataAccessException for null request");
    }
}