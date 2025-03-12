package dataaccess;

import dataaccess.sql.MySqlUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDaoTest {

    private MySqlUserDao testDataBase;

    @BeforeEach
    void setUp() throws DataAccessException {
        testDataBase = new MySqlUserDao();
        testDataBase.clear();
    }

    @Test
    void clear() {
    }

    @Test
    void createUserPositive() {
    }

    @Test
    void createUserNegative() {
    }

    @Test
    void getUserPositive() {
    }

    @Test
    void getUserNegative() {
    }

    @Test
    void verifyUserPositive() {
    }

    @Test
    void verifyUserNegative() {
    }
}