package ro.atrifan.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import ro.atrifan.model.ActivityState;
import ro.atrifan.model.LoginModel;
import ro.atrifan.persistence.dao.GroupUserDao;
import ro.atrifan.persistence.dao.UserDao;
import ro.atrifan.persistence.entities.User;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Created by alexandru.trifan on 09.05.2016.
 */

@RunWith(MockitoJUnitRunner.class)
public class ClientControllerTest {
    @Mock
    UserDao userDao;

    @Mock
    GroupUserDao groupUserDao;

    @InjectMocks
    ClientController clientControllerToTest;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoginOkWithExistingUser() {

        User userToReturn = new User();
        userToReturn.setColor("WHITE");
        userToReturn.setUserName("GIGI");
        userToReturn.setPassword("GIGI");
        userToReturn.setState(ActivityState.OFFLINE);

        Mockito.doReturn(userToReturn).when(userDao).findByName("GIGI");

        LoginModel loginModel = new LoginModel();
        loginModel.setUserName("GIGI");
        loginModel.setPassword("GIGI");
        Response response = clientControllerToTest.login(loginModel);

        assertEquals(response.getStatus(), 200);
        //can verify also if queue was called with event and the response is what we expect
        Mockito.verify(userDao).update(Mockito.any(User.class));
        assertEquals(userToReturn.getState(), ActivityState.CONNECTED);

    }

    @Test
    public void testLoginWithWrongPassword() {

        User userToReturn = new User();
        userToReturn.setColor("WHITE");
        userToReturn.setUserName("GIGI");
        userToReturn.setPassword("GIGI");
        userToReturn.setState(ActivityState.OFFLINE);

        Mockito.doReturn(userToReturn).when(userDao).findByName("GIGI");

        LoginModel loginModel = new LoginModel();
        loginModel.setUserName("GIGI");
        loginModel.setPassword("GIGI2");
        Response response = clientControllerToTest.login(loginModel);

        assertEquals(response.getStatus(), 401);
        //can verify also if queue was called with event and the response is what we expect
    }

    @Test
    public void testLoginWithNewUser() {

        Mockito.doReturn(null).when(userDao).findByName("GIGI");

        LoginModel loginModel = new LoginModel();
        loginModel.setUserName("GIGI");
        loginModel.setPassword("GIGI2");
        Response response = clientControllerToTest.login(loginModel);

        assertEquals(response.getStatus(), 200);
        Mockito.verify(userDao).persist(Mockito.any(User.class));
        //can verify also if queue was called with event and the response is what we expect
    }

    @Test
    public void testLoginWithCorrectUserButAlreadyConnected() {

        User userToReturn = new User();
        userToReturn.setColor("WHITE");
        userToReturn.setUserName("GIGI");
        userToReturn.setPassword("GIGI");
        userToReturn.setState(ActivityState.CONNECTED);

        Mockito.doReturn(userToReturn).when(userDao).findByName("GIGI");

        LoginModel loginModel = new LoginModel();
        loginModel.setUserName("GIGI");
        loginModel.setPassword("GIGI");
        Response response = clientControllerToTest.login(loginModel);

        assertEquals(response.getStatus(), Response.Status.FOUND.getStatusCode());
        //can verify also if queue was called with event and the response is what we expect
    }

}

