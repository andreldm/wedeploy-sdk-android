package com.wedeploy.android;

import com.wedeploy.android.auth.Authorization;
import com.wedeploy.android.auth.TokenAuthorization;
import com.wedeploy.android.exception.WeDeployException;
import com.wedeploy.android.transport.OkHttpTransport;
import com.wedeploy.android.transport.RequestMethod;
import com.wedeploy.android.transport.Request;
import com.wedeploy.android.transport.Response;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.wedeploy.android.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Silvio Santos
 */
public class WeDeployAuthTest {

	@BeforeClass
	public static void setUpBeforeClass() throws WeDeployException {
		deleteUsers();
		USER_ID = createUser(EMAIL, PASSWORD, NAME);
	}

	@Test
	public void signIn() throws Exception {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");

		assertNotNull(token);
	}

	@Test(expected = IllegalArgumentException.class)
	public void signIn_withNullEmail_shouldThrowException() {
		weDeploy.auth(AUTH_URL)
			.authorization(AUTHORIZATION)
			.signIn(null, PASSWORD);
	}

	@Test(expected = IllegalArgumentException.class)
	public void signIn_withNullPassword_shouldThrowException() {
		weDeploy.auth(AUTH_URL)
			.authorization(AUTHORIZATION)
			.signIn(EMAIL, null);
	}

	@Test
	public void signOut() throws WeDeployException {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");

		weDeploy.auth(AUTH_URL)
			.authorization(new TokenAuthorization(token))
			.signOut()
			.execute();
	}

	@Test
	public void deleteUser() throws WeDeployException {
		String id = createUser("test@wedeploy.me", "123456", "Test Test");

		weDeploy.auth(AUTH_URL)
			.authorization(AUTHORIZATION)
			.deleteUser(id)
			.execute();
	}

	@Test
	public void updateUser() throws Exception {
		Response response = weDeploy.auth(AUTH_URL)
			.signIn(EMAIL, PASSWORD)
			.execute();

		JSONObject jsonBody = new JSONObject(response.getBody());
		String token = jsonBody.getString("access_token");
		Authorization authorization = new TokenAuthorization(token);

		JSONObject fields = new JSONObject();
		fields.put("name", "Silvio Santos 2");

		weDeploy.auth(AUTH_URL)
			.authorization(authorization)
			.updateUser(USER_ID, fields)
			.execute();

		response = weDeploy.auth(AUTH_URL)
			.authorization(authorization)
			.getCurrentUser()
			.execute();

		jsonBody = new JSONObject(response.getBody());
		assertEquals("Silvio Santos 2", jsonBody.getString("name"));

		fields.put("name", NAME);
		weDeploy.auth(AUTH_URL)
			.authorization(authorization)
			.updateUser(USER_ID, fields)
			.execute();
	}

	@Test
	public void sendPasswordResetEmail() throws WeDeployException {
		weDeploy.auth(AUTH_URL)
			.sendPasswordResetEmail(EMAIL)
			.execute();
	}

	private static String createUser(String email, String password, String name)
		throws WeDeployException {

		Response response = weDeploy.auth(AUTH_URL)
			.createUser(email, password, name)
			.execute();

		assertEquals(200, response.getStatusCode());

		JSONObject jsonObject = new JSONObject(response.getBody());

		return jsonObject.getString("id");
	}

	private static void deleteUsers() {
		try {
			Request.Builder builder = new Request.Builder()
				.url(AUTH_URL)
				.method(RequestMethod.DELETE)
				.header("Authorization", "Bearer " + MASTER_TOKEN)
				.path("users");

			OkHttpTransport transport = new OkHttpTransport.Builder().build();

			Call<Response> call = new Call<>(
				builder.build(), transport, transport, Response.class);

			call.execute();
		}
		catch (Exception e) {
		}
	}

	private static WeDeploy weDeploy = new WeDeploy.Builder().build();

}